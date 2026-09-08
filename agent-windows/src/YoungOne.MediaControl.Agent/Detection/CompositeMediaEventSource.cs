using System.Threading.Channels;
using YoungOne.MediaControl.Agent.Policy;
namespace YoungOne.MediaControl.Agent.Detection;
public sealed class CompositeMediaEventSource(WindowsMediaEventSource inventory,KernelMinifilterEventSource kernel):IMediaEventSource{
 public async IAsyncEnumerable<DetectedMedia> WatchAsync([System.Runtime.CompilerServices.EnumeratorCancellation]CancellationToken ct){var channel=Channel.CreateBounded<DetectedMedia>(new BoundedChannelOptions(2048){FullMode=BoundedChannelFullMode.DropOldest,SingleReader=true});Task a=Pump(inventory,channel.Writer,ct);Task b=Pump(kernel,channel.Writer,ct);await foreach(var item in channel.Reader.ReadAllAsync(ct))yield return item;await Task.WhenAll(a,b);}
 static async Task Pump(IMediaEventSource source,ChannelWriter<DetectedMedia> writer,CancellationToken ct){try{await foreach(var item in source.WatchAsync(ct))await writer.WriteAsync(item,ct);}catch(OperationCanceledException)when(ct.IsCancellationRequested){}}
}
