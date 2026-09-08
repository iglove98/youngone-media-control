using System.Diagnostics;
using System.Drawing;
using System.IO.Pipes;
using System.Runtime.InteropServices;
using System.Security.Principal;
using System.Text.Json;
namespace YoungOne.MediaControl.UserUi;
internal sealed record UserMessage(string Type,string? MediaType,string? Decision,bool EnforcementApplied,string? EnforcementResultCode,string? ReasonCode,string? Connectivity,string? ControlStatus,string? Detail,DateTimeOffset OccurredAt);
internal static class Program{
 [STAThread]static void Main(){ApplicationConfiguration.Initialize();Application.Run(new TrayContext(GetSessionId()));}
 static int GetSessionId(){if(!ProcessIdToSessionId((uint)Environment.ProcessId,out uint id))throw new InvalidOperationException("Cannot resolve Windows session");return checked((int)id);}
 [DllImport("kernel32.dll",SetLastError=true)][return:MarshalAs(UnmanagedType.Bool)]static extern bool ProcessIdToSessionId(uint processId,out uint sessionId);
}
internal sealed class TrayContext:ApplicationContext{
 readonly NotifyIcon tray;readonly Control dispatcher=new();readonly Dictionary<string,Icon> icons;readonly CancellationTokenSource stop=new();readonly int sessionId;string state="NEVER_CONNECTED";
 public TrayContext(int session){sessionId=session;dispatcher.CreateControl();icons=new(){["ONLINE"]=CreateIcon(Color.LimeGreen),["OFFLINE_LAST_POLICY"]=CreateIcon(Color.Goldenrod),["OFFLINE_DETECT_ONLY"]=CreateIcon(Color.Gray),["NEVER_CONNECTED"]=CreateIcon(Color.DimGray),["FAILSAFE"]=CreateIcon(Color.Red),["DRIVER_ERROR"]=CreateIcon(Color.OrangeRed)};tray=new NotifyIcon{Visible=true,Icon=icons[state],Text="YoungOne 매체제어: 서버 미연결",ContextMenuStrip=BuildMenu()};_ = ListenAsync(stop.Token);}
 ContextMenuStrip BuildMenu(){var menu=new ContextMenuStrip();menu.Items.Add("상태 보기",null,(_,_)=>MessageBox.Show(StatusText(),"YoungOne 매체제어",MessageBoxButtons.OK,MessageBoxIcon.Information));menu.Items.Add("종료",null,(_,_)=>ExitThread());return menu;}
 async Task ListenAsync(CancellationToken ct){string pipeName="YoungOne.MediaControl.Notifications."+sessionId;while(!ct.IsCancellationRequested){try{await using var pipe=new NamedPipeServerStream(pipeName,PipeDirection.In,1,PipeTransmissionMode.Byte,PipeOptions.Asynchronous);await pipe.WaitForConnectionAsync(ct);if(!IsLocalSystemClient(pipe)){pipe.Disconnect();continue;}using var reader=new StreamReader(pipe);string? json=await reader.ReadLineAsync(ct);if(string.IsNullOrWhiteSpace(json))continue;UserMessage? message=JsonSerializer.Deserialize<UserMessage>(json,new JsonSerializerOptions{PropertyNameCaseInsensitive=true});if(message is null)continue;if(message.Type=="STATUS")dispatcher.BeginInvoke(()=>UpdateStatus(message));else if(message.Type=="RISK")dispatcher.BeginInvoke(()=>ShowRisk(message));}catch(OperationCanceledException)when(ct.IsCancellationRequested){break;}catch(IOException){await Task.Delay(1000,ct);}catch(JsonException){await Task.Delay(1000,ct);}}}
 void UpdateStatus(UserMessage message){state=message.Connectivity??"NEVER_CONNECTED";if(message.ControlStatus is "FAILSAFE" or "DRIVER_ERROR")state=message.ControlStatus;tray.Icon=icons.GetValueOrDefault(state,icons["NEVER_CONNECTED"]);tray.Text=TrimTooltip("YoungOne 매체제어: "+StatusLabel());}
 void ShowRisk(UserMessage n){string result=n.EnforcementApplied?"실제 통제가 적용되었습니다.":"정책상 제한 대상이지만 드라이버 통제는 적용되지 않았습니다.";string text=$"매체: {n.MediaType}\n정책 결정: {n.Decision}\n결과: {result}\n코드: {n.EnforcementResultCode}\n사유: {n.ReasonCode}";MessageBox.Show(text,"YoungOne 매체제어",MessageBoxButtons.OK,n.EnforcementApplied?MessageBoxIcon.Stop:MessageBoxIcon.Warning);}
 string StatusLabel()=>state switch{"ONLINE"=>"온라인","OFFLINE_LAST_POLICY"=>"오프라인 - 마지막 정책 적용","OFFLINE_DETECT_ONLY"=>"오프라인 - 탐지만 수행","FAILSAFE"=>"보호 모드","DRIVER_ERROR"=>"드라이버 오류",_=>"서버 미연결 - 탐지만 수행"};
 string StatusText()=>StatusLabel()+"\n\n온라인: 초록\n오프라인·마지막 정책: 노랑\n미연결·탐지 전용: 회색\n오류·보호 모드: 빨강";
 static string TrimTooltip(string value)=>value.Length<=63?value:value[..63];
 static bool IsLocalSystemClient(NamedPipeServerStream pipe){bool allowed=false;pipe.RunAsClient(()=>{using WindowsIdentity identity=WindowsIdentity.GetCurrent();allowed=identity.User?.IsWellKnown(WellKnownSidType.LocalSystemSid)==true;});return allowed;}
 static Icon CreateIcon(Color color){using var bitmap=new Bitmap(16,16);using Graphics g=Graphics.FromImage(bitmap);g.Clear(Color.Transparent);using var brush=new SolidBrush(color);using var pen=new Pen(Color.Black);g.FillEllipse(brush,2,2,12,12);g.DrawEllipse(pen,2,2,12,12);IntPtr handle=bitmap.GetHicon();using Icon temporary=Icon.FromHandle(handle);Icon result=(Icon)temporary.Clone();DestroyIcon(handle);return result;}
 protected override void ExitThreadCore(){stop.Cancel();tray.Visible=false;tray.Dispose();foreach(Icon icon in icons.Values)icon.Dispose();dispatcher.Dispose();stop.Dispose();base.ExitThreadCore();}
 [DllImport("user32.dll",SetLastError=true)][return:MarshalAs(UnmanagedType.Bool)]static extern bool DestroyIcon(IntPtr handle);
}