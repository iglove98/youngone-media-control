using YoungOne.MediaControl.Agent.Detection;
using Xunit;
namespace YoungOne.MediaControl.Agent.Tests;
public sealed class KernelMinifilterEventSourceTests{
 [Theory][InlineData((byte)0,MediaOperation.Open)][InlineData((byte)2,MediaOperation.Read)][InlineData((byte)3,MediaOperation.Write)][InlineData((byte)6,MediaOperation.Write)][InlineData((byte)255,MediaOperation.Execute)]public void MapsRegisteredFilterOperations(byte major,MediaOperation expected)=>Assert.Equal(expected,KernelMinifilterEventSource.Map(major));
 [Fact]public void IgnoresUnregisteredOperation()=>Assert.Null(KernelMinifilterEventSource.Map(4));
 [Fact]public void AbiV2EventHasExpectedNativeSize()=>Assert.Equal(168,KernelMinifilterEventSource.EventSize);
}
