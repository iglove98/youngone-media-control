using YoungOne.MediaControl.Agent.Policy;
namespace YoungOne.MediaControl.Agent.Enforcement;
public sealed class DriverPolicySynchronizer(DriverControlClient driver,ILogger<DriverPolicySynchronizer> logger){
 public bool Apply(PolicyEnvelope envelope){byte[] packet=DriverPolicyPacketBuilder.Build(envelope);bool accepted=driver.TrySetPolicy(packet,out string resultCode);if(accepted)logger.LogInformation("Policy {PolicyVersion} sent to driver: {ResultCode}",envelope.PolicyVersion,resultCode);else logger.LogWarning("Policy {PolicyVersion} was cached but not accepted by driver: {ResultCode}",envelope.PolicyVersion,resultCode);return accepted;}
}