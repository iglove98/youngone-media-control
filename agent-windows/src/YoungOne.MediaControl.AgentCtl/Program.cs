using System.Security;
using System.Text.Json;
using Microsoft.Extensions.Options;
using YoungOne.MediaControl.Agent;
using YoungOne.MediaControl.Agent.Identity;
using YoungOne.MediaControl.Agent.Security;
if(args.Length<1){Console.Error.WriteLine("Usage: authorize-uninstall <token-file> | consume-uninstall-approval");return 64;}
try{
 string configPath=Path.Combine(AppContext.BaseDirectory,"appsettings.json");AgentOptions options=new();if(File.Exists(configPath)){using JsonDocument doc=JsonDocument.Parse(File.ReadAllText(configPath));if(doc.RootElement.TryGetProperty("Agent",out JsonElement agent)){if(agent.TryGetProperty("DataDirectory",out JsonElement data))options.DataDirectory=data.GetString()??options.DataDirectory;if(agent.TryGetProperty("PinnedUninstallPublicKey",out JsonElement key))options.PinnedUninstallPublicKey=key.GetString();}}
 var configured=Options.Create(options);var credential=new AgentCredentialStore(configured);var identity=new InstallationIdentityStore(configured);var store=new UninstallAuthorizationStore(configured,credential,identity);
 if(args[0]=="authorize-uninstall"&&args.Length==2){var envelope=JsonSerializer.Deserialize<UninstallTokenEnvelope>(File.ReadAllText(args[1]),new JsonSerializerOptions{PropertyNameCaseInsensitive=true})??throw new InvalidDataException("Invalid token file");store.Authorize(envelope,DateTimeOffset.UtcNow);Console.WriteLine("Uninstall authorization stored.");return 0;}
 if(args[0]=="consume-uninstall-approval"&&args.Length==1){store.Consume(DateTimeOffset.UtcNow);Console.WriteLine("Uninstall authorization consumed.");return 0;}
 Console.Error.WriteLine("Invalid arguments.");return 64;
}catch(Exception ex)when(ex is SecurityException or IOException or JsonException or FormatException){Console.Error.WriteLine(ex.Message);return 77;}