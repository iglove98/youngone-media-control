namespace YoungOne.MediaControl.Agent.Detection;
public sealed class KernelEventRateGate(TimeSpan interval,int capacity=4096){
 readonly Dictionary<string,DateTimeOffset> seen=new(StringComparer.OrdinalIgnoreCase);
 public bool ShouldEmit(string? volume,uint processId,byte major,DateTimeOffset now){string key=$"{volume}|{processId}|{major}";if(seen.TryGetValue(key,out var last)&&now-last<interval)return false;if(seen.Count>=capacity){DateTimeOffset cutoff=now-interval;foreach(string stale in seen.Where(x=>x.Value<cutoff).Select(x=>x.Key).Take(Math.Max(1,capacity/4)).ToArray())seen.Remove(stale);if(seen.Count>=capacity)seen.Remove(seen.Keys.First());}seen[key]=now;return true;}
}
