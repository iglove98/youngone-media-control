using Xunit;
using YoungOne.MediaControl.Agent.Detection;
namespace YoungOne.MediaControl.Agent.Tests;
public sealed class UserNotificationTests{
 [Theory]
 [InlineData(1,"YoungOne.MediaControl.Notifications.1")]
 [InlineData(42,"YoungOne.MediaControl.Notifications.42")]
 public void PipeNameIsSessionScoped(int sessionId,string expected)=>Assert.Equal(expected,NamedPipeUserNotifier.PipeName(sessionId));
}