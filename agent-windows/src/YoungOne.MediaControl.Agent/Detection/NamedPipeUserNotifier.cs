using System.IO.Pipes;
using System.Runtime.InteropServices;
using System.Security.Principal;
using System.Text.Json;
using YoungOne.MediaControl.Agent.Enforcement;
using YoungOne.MediaControl.Agent.Policy;
namespace YoungOne.MediaControl.Agent.Detection;
public sealed record UserMessage(string Type,string? MediaType,string? Decision,bool EnforcementApplied,string? EnforcementResultCode,string? ReasonCode,string? Connectivity,string? ControlStatus,string? Detail,DateTimeOffset OccurredAt);
public interface IUserNotifier{
 Task<bool> NotifyAsync(DetectedMedia media,PolicyDecision decision,EnforcementResult result,DateTimeOffset occurredAt,CancellationToken ct);
 Task<bool> PublishStatusAsync(string connectivity,string controlStatus,string detail,CancellationToken ct);
}
public sealed class NamedPipeUserNotifier:IUserNotifier{
 public Task<bool> NotifyAsync(DetectedMedia media,PolicyDecision decision,EnforcementResult result,DateTimeOffset occurredAt,CancellationToken ct)=>decision.Decision==EnforcementDecision.Allow?Task.FromResult(false):SendAsync(new("RISK",media.MediaType,decision.Decision.ToString(),result.Applied,result.ResultCode,decision.ReasonCode,null,null,null,occurredAt),ct);
 public Task<bool> PublishStatusAsync(string connectivity,string controlStatus,string detail,CancellationToken ct)=>SendAsync(new("STATUS",null,null,false,null,null,connectivity,controlStatus,detail,DateTimeOffset.UtcNow),ct);
 async Task<bool> SendAsync(UserMessage message,CancellationToken ct){bool delivered=false;string json=JsonSerializer.Serialize(message);foreach(int sessionId in ActiveSessions()){try{await using var pipe=new NamedPipeClientStream(".",PipeName(sessionId),PipeDirection.Out,PipeOptions.Asynchronous,TokenImpersonationLevel.Identification);await pipe.ConnectAsync(200,ct);await using var writer=new StreamWriter(pipe){AutoFlush=true};await writer.WriteLineAsync(json.AsMemory(),ct);delivered=true;}catch(TimeoutException){}catch(IOException){}}return delivered;}
 public static string PipeName(int sessionId)=>"YoungOne.MediaControl.Notifications."+sessionId;
 static List<int> ActiveSessions(){var result=new List<int>();if(!WTSEnumerateSessions(IntPtr.Zero,0,1,out IntPtr buffer,out int count))return result;try{int size=Marshal.SizeOf<WtsSessionInfo>();for(int i=0;i<count;i++){var item=Marshal.PtrToStructure<WtsSessionInfo>(IntPtr.Add(buffer,i*size));if(item.State==0)result.Add(item.SessionId);}}finally{WTSFreeMemory(buffer);}return result;}
 [StructLayout(LayoutKind.Sequential)]struct WtsSessionInfo{public int SessionId;public IntPtr StationName;public int State;}
 [DllImport("wtsapi32.dll",SetLastError=true)][return:MarshalAs(UnmanagedType.Bool)]static extern bool WTSEnumerateSessions(IntPtr server,int reserved,int version,out IntPtr sessions,out int count);
 [DllImport("wtsapi32.dll")]static extern void WTSFreeMemory(IntPtr memory);
}