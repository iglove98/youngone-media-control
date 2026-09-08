using YoungOne.MediaControl.Agent.Detection;
using Xunit;
namespace YoungOne.MediaControl.Agent.Tests;
public sealed class KernelEventRateGateTests{
 [Fact]public void CoalescesSameVolumeProcessAndOperation(){var gate=new KernelEventRateGate(TimeSpan.FromSeconds(1));var now=DateTimeOffset.UtcNow;Assert.True(gate.ShouldEmit("V",10,3,now));Assert.False(gate.ShouldEmit("V",10,3,now.AddMilliseconds(999)));Assert.True(gate.ShouldEmit("V",10,3,now.AddSeconds(1)));}
 [Fact]public void KeepsDifferentProcessesAndOperations(){var gate=new KernelEventRateGate(TimeSpan.FromSeconds(1));var now=DateTimeOffset.UtcNow;Assert.True(gate.ShouldEmit("V",10,2,now));Assert.True(gate.ShouldEmit("V",11,2,now));Assert.True(gate.ShouldEmit("V",10,3,now));}
}
