using System.Text.Json;
using YoungOne.MediaControl.Agent.Detection;
namespace YoungOne.MediaControl.Agent.Policy;
public sealed record DetectedMedia(string MediaType,string? VendorId,string? ProductId,string? SerialHash,string? DeviceInstanceId,MediaOperation Operation,string? UserId);
public sealed record PolicyDecision(EnforcementDecision Decision,string ReasonCode,Guid? PolicyId,long? PolicyVersion,bool IsOfflineFallback);
public sealed class PolicyDecisionEngine(PolicyCache cache){
 static readonly JsonSerializerOptions JsonOptions=new(){PropertyNameCaseInsensitive=true};
 public PolicyDecision Evaluate(DetectedMedia media,DateTimeOffset now){
  bool activated;try{activated=cache.HasEverActivated();}catch{return FailClosed(media,"ACTIVATION_LATCH_INVALID");}
  PolicyEnvelope? envelope;try{envelope=cache.Load();}catch{return FailClosed(media,"POLICY_CACHE_INVALID");}
  if(envelope is null)return activated?FailClosed(media,"ACTIVATED_POLICY_MISSING"):Bootstrap(media,"POLICY_NOT_YET_ASSIGNED");
  if(now<envelope.EffectiveAt)return Bootstrap(media,"POLICY_NOT_EFFECTIVE");
  if(envelope.ExpiresAt is not null&&now>=envelope.ExpiresAt)return FailClosed(media,"POLICY_EXPIRED");
  PolicyPayload? payload;try{payload=JsonSerializer.Deserialize<PolicyPayload>(envelope.PayloadJson,JsonOptions);}catch(JsonException){return FailClosed(media,"POLICY_PAYLOAD_INVALID");}
  if(payload is null||payload.SchemaVersion!=2)return FailClosed(media,"POLICY_SCHEMA_UNSUPPORTED");
  PolicyRule? rule=payload.Rules.Where(x=>Match(x,media)).OrderByDescending(x=>(x.VendorId is null?0:1)+(x.ProductId is null?0:1)+(x.SerialHash is null?0:1)).FirstOrDefault();
  if(rule is null)return Bootstrap(media,"NO_MATCHING_RULE");
  string action=media.Operation switch{MediaOperation.Write or MediaOperation.Format=>rule.WriteAction,MediaOperation.Execute=>rule.ExecuteAction,_=>rule.ReadAction};
  return new(Parse(action),"POLICY_RULE_MATCH",envelope.PolicyId,envelope.PolicyVersion,false);
 }
 static PolicyDecision Bootstrap(DetectedMedia media,string reason)=>new(EnforcementDecision.DetectOnly,reason+"_DETECT_ONLY",null,null,true);
 static PolicyDecision FailClosed(DetectedMedia media,string reason)=>IsEssentialHid(media.MediaType)?new(EnforcementDecision.Allow,reason+"_ESSENTIAL_HID_ALLOW",null,null,true):new(EnforcementDecision.Block,reason+"_FAIL_CLOSED",null,null,true);
 static bool Match(PolicyRule r,DetectedMedia m)=>Eq(r.MediaType,m.MediaType)&&Opt(r.VendorId,m.VendorId)&&Opt(r.ProductId,m.ProductId)&&Opt(r.SerialHash,m.SerialHash);
 static bool Eq(string? a,string? b)=>string.Equals(a?.Trim(),b?.Trim(),StringComparison.OrdinalIgnoreCase);static bool Opt(string? a,string? b)=>string.IsNullOrWhiteSpace(a)||Eq(a,b);
 static bool IsEssentialHid(string mediaType)=>mediaType is "KEYBOARD" or "MOUSE" or "HID_ESSENTIAL";
 static EnforcementDecision Parse(string? action)=>action?.ToUpperInvariant() switch{"ALLOW"=>EnforcementDecision.Allow,"READ_ONLY"=>EnforcementDecision.ReadOnly,"DETECT_ONLY"=>EnforcementDecision.DetectOnly,_=>EnforcementDecision.Block};
 sealed record PolicyPayload(long SchemaVersion,long PolicyVersion,List<PolicyRule> Rules,DateTimeOffset EffectiveAt,DateTimeOffset? ExpiresAt);
 sealed record PolicyRule(string MediaType,string? VendorId,string? ProductId,string? SerialHash,string ReadAction,string WriteAction,string ExecuteAction,string OfflineBehavior,long OfflineGraceSeconds);
}