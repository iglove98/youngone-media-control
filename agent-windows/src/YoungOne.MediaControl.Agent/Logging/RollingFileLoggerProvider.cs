using System.Text.Json;
using Microsoft.Extensions.Options;
namespace YoungOne.MediaControl.Agent.Logging;
public sealed class RollingFileLoggerProvider:ILoggerProvider{
 readonly string directory;readonly long maxBytes;readonly int retentionDays;readonly object gate=new();readonly Dictionary<string,RollingFileLogger> loggers=new(StringComparer.Ordinal);
 public RollingFileLoggerProvider(IOptions<AgentOptions> options){directory=Path.Combine(options.Value.DataDirectory,"logs");maxBytes=options.Value.LogMaxFileMb*1024L*1024L;retentionDays=options.Value.LogRetentionDays;Directory.CreateDirectory(directory);DeleteExpired();}
 public ILogger CreateLogger(string categoryName){lock(gate){if(!loggers.TryGetValue(categoryName,out RollingFileLogger? logger)){logger=new(categoryName,Write);loggers.Add(categoryName,logger);}return logger;}}
 void Write(string category,LogLevel level,EventId eventId,string message,Exception? exception){try{lock(gate){string path=CurrentPath();var entry=new{timestamp=DateTimeOffset.UtcNow,level=level.ToString(),category,eventId=eventId.Id,message,exception=exception?.ToString()};File.AppendAllText(path,JsonSerializer.Serialize(entry)+Environment.NewLine,System.Text.Encoding.UTF8);}}catch(IOException){}catch(UnauthorizedAccessException){}}
 string CurrentPath(){string date=DateTime.UtcNow.ToString("yyyyMMdd",System.Globalization.CultureInfo.InvariantCulture);string basePath=Path.Combine(directory,$"agent-{date}.log");if(!File.Exists(basePath)||new FileInfo(basePath).Length<maxBytes)return basePath;for(int i=1;i<=999;i++){string candidate=Path.Combine(directory,$"agent-{date}-{i:D2}.log");if(!File.Exists(candidate)||new FileInfo(candidate).Length<maxBytes)return candidate;}throw new IOException("Daily Agent log segment limit exceeded");}
 void DeleteExpired(){DateTime cutoff=DateTime.UtcNow.AddDays(-retentionDays);foreach(string file in Directory.EnumerateFiles(directory,"agent-*.log")){try{if(File.GetLastWriteTimeUtc(file)<cutoff)File.Delete(file);}catch(IOException){}catch(UnauthorizedAccessException){}}}
 public void Dispose(){lock(gate){loggers.Clear();}}
 sealed class RollingFileLogger(string category,Action<string,LogLevel,EventId,string,Exception?> write):ILogger{
  public IDisposable? BeginScope<TState>(TState state)where TState:notnull=>NullScope.Instance;
  public bool IsEnabled(LogLevel logLevel)=>logLevel!=LogLevel.None;
  public void Log<TState>(LogLevel level,EventId eventId,TState state,Exception? exception,Func<TState,Exception?,string> formatter){if(IsEnabled(level))write(category,level,eventId,formatter(state,exception),exception);}
 }
 sealed class NullScope:IDisposable{public static readonly NullScope Instance=new();public void Dispose(){}}
}