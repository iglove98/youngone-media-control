using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Xunit;
using YoungOne.MediaControl.Agent;
using YoungOne.MediaControl.Agent.Detection;
using YoungOne.MediaControl.Agent.Logging;
using YoungOne.MediaControl.Agent.Storage;
namespace YoungOne.MediaControl.Agent.Tests;
public sealed class LocalStorageTests{
 [Fact]public void RollingLoggerWritesJsonLogToConfiguredDirectory(){using var temp=new TempDir();using var provider=new RollingFileLoggerProvider(Options.Create(new AgentOptions{DataDirectory=temp.Path,LogRetentionDays=30,LogMaxFileMb=1}));ILogger logger=provider.CreateLogger("test");logger.LogInformation("storage-test");string file=Assert.Single(Directory.GetFiles(Path.Combine(temp.Path,"logs"),"agent-*.log"));string content=File.ReadAllText(file);Assert.Contains("storage-test",content,StringComparison.Ordinal);Assert.Contains("\"level\":\"Information\"",content,StringComparison.Ordinal);}
 [Fact]public async Task RiskQueuePersistsAndCountsPendingEvent(){using var temp=new TempDir();var queue=new RiskEventQueue(Options.Create(new AgentOptions{DataDirectory=temp.Path}));var item=new RiskEventRequest("event-1",new string('a',64),"user","device",new string('b',64),"USB_STORAGE","Connect","DetectOnly","TEST",null,null,false,"NO_RESTRICTION_REQUIRED",false,DateTimeOffset.UtcNow);await queue.EnqueueAsync(item,CancellationToken.None);Assert.Equal(1,await queue.CountAsync(CancellationToken.None));Assert.True(File.Exists(Path.Combine(temp.Path,"risk-queue.db")));}
 sealed class TempDir:IDisposable{public string Path{get;}=System.IO.Path.Combine(System.IO.Path.GetTempPath(),"ymc-"+Guid.NewGuid());public TempDir()=>Directory.CreateDirectory(Path);public void Dispose()=>Directory.Delete(Path,true);}
}