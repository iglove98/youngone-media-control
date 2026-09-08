using YoungOne.MediaControl.Agent.Policy;
using YoungOne.MediaControl.Agent.Detection;
namespace YoungOne.MediaControl.Agent.Enforcement;
public sealed record EnforcementResult(bool Applied,string ResultCode,string? Detail);
public interface IMediaEnforcer{Task<EnforcementResult> ApplyAsync(DetectedMedia media,PolicyDecision decision,CancellationToken ct);}
public sealed class DriverMediaEnforcer(DriverControlClient driver):IMediaEnforcer{
 public Task<EnforcementResult> ApplyAsync(DetectedMedia media,PolicyDecision decision,CancellationToken ct){
  if(decision.Decision is EnforcementDecision.Allow or EnforcementDecision.DetectOnly)return Task.FromResult(new EnforcementResult(true,"NO_RESTRICTION_REQUIRED",null));
  if(!driver.TryQueryStatus(out DriverStatus status,out string code))return Task.FromResult(new EnforcementResult(false,code,"Desired restriction was not applied."));
  if(status.EnforcementReady==0)return Task.FromResult(new EnforcementResult(false,"DRIVER_ENFORCEMENT_NOT_READY","Driver contract is available, but device enforcement is intentionally disabled."));
  return Task.FromResult(new EnforcementResult(false,"ENFORCEMENT_COMMAND_NOT_IMPLEMENTED","Policy decision is not yet translated into a driver command."));
 }
}