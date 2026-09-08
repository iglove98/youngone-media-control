using YoungOne.MediaControl.Agent.Enforcement;
using YoungOne.MediaControl.Agent.Policy;
namespace YoungOne.MediaControl.Agent;
public sealed record AgentRuntimeSnapshot(long QueueDepth,string? PolicyVersion,string ControlStatus);
public sealed class AgentRuntimeStatusProvider(PolicyCache policies,DriverControlClient driver,Storage.RiskEventQueue queue){
 public async Task<AgentRuntimeSnapshot> GetAsync(CancellationToken ct){long depth=await queue.CountAsync(ct);bool activated;try{activated=policies.HasEverActivated();}catch{return new(depth,null,"FAILSAFE");}PolicyEnvelope? policy;try{policy=policies.Load();}catch{return new(depth,null,"FAILSAFE");}if(!activated&&policy is null)return new(depth,null,"BOOTSTRAP_DETECT_ONLY");if(policy is null||policy.ExpiresAt is not null&&DateTimeOffset.UtcNow>=policy.ExpiresAt)return new(depth,null,"FAILSAFE");string version=policy.PolicyVersion.ToString(System.Globalization.CultureInfo.InvariantCulture);if(!driver.TryQueryStatus(out DriverStatus status,out _)||status.EnforcementReady==0)return new(depth,version,"DRIVER_ERROR");return new(depth,version,"ENFORCING");}
}