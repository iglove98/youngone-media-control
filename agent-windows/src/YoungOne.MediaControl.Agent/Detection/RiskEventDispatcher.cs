using YoungOne.MediaControl.Agent.Identity;
using YoungOne.MediaControl.Agent.Storage;
using YoungOne.MediaControl.Agent.Transport;
namespace YoungOne.MediaControl.Agent.Detection;
public sealed class RiskEventDispatcher(RiskEventQueue queue,MediaControlClient client,AgentCredentialStore credentials,ILogger<RiskEventDispatcher> logger):BackgroundService{
 protected override async Task ExecuteAsync(CancellationToken ct){while(!ct.IsCancellationRequested){var credential=credentials.Load();if(credential is null){await Task.Delay(TimeSpan.FromSeconds(5),ct);continue;}foreach(var item in await queue.DueAsync(50,ct)){try{await client.SendRiskEventAsync(credential,item.Event,ct);await queue.SentAsync(item.Event.EventId,ct);}catch(OperationCanceledException)when(ct.IsCancellationRequested){return;}catch(Exception ex){logger.LogWarning(ex,"Risk event {EventId} send failed",item.Event.EventId);await queue.FailedAsync(item.Event.EventId,ex.Message,item.AttemptCount,ct);}}await Task.Delay(TimeSpan.FromSeconds(2),ct);}}
}