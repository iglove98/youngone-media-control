using Xunit;
using Microsoft.Extensions.Options;
using YoungOne.MediaControl.Agent;
using YoungOne.MediaControl.Agent.Detection;
using YoungOne.MediaControl.Agent.Policy;
namespace YoungOne.MediaControl.Agent.Tests;
public sealed class PolicyDecisionEngineTests{
 [Fact] public void MissingPolicyDetectsUsbStorageWithoutBlocking(){using var temp=new TempDir();var engine=Create(temp.Path,false);var result=engine.Evaluate(new("USB_STORAGE",null,null,null,"USB\\TEST",MediaOperation.Write,"user"),DateTimeOffset.UtcNow);Assert.Equal(EnforcementDecision.DetectOnly,result.Decision);Assert.True(result.IsOfflineFallback);}
 [Fact] public void MissingPolicyDetectsEssentialKeyboardWithoutBlocking(){using var temp=new TempDir();var engine=Create(temp.Path,false);var result=engine.Evaluate(new("KEYBOARD",null,null,null,"HID\\TEST",MediaOperation.Connect,"user"),DateTimeOffset.UtcNow);Assert.Equal(EnforcementDecision.DetectOnly,result.Decision);}
 [Fact] public void ActivatedAgentWithDeletedPolicyFailsClosed(){using var temp=new TempDir();var engine=Create(temp.Path,true);var result=engine.Evaluate(new("USB_STORAGE",null,null,null,"USB\\TEST",MediaOperation.Write,"user"),DateTimeOffset.UtcNow);Assert.Equal(EnforcementDecision.Block,result.Decision);}
 [Fact] public void ActivatedAgentStillAllowsEssentialHidDuringFailsafe(){using var temp=new TempDir();var engine=Create(temp.Path,true);var result=engine.Evaluate(new("KEYBOARD",null,null,null,"HID\\TEST",MediaOperation.Connect,"user"),DateTimeOffset.UtcNow);Assert.Equal(EnforcementDecision.Allow,result.Decision);}
 static PolicyDecisionEngine Create(string path,bool activated)=>new(new FakePolicyCache(Options.Create(new AgentOptions{DataDirectory=path}),activated));
 sealed class FakePolicyCache(IOptions<AgentOptions> options,bool activated):PolicyCache(options){public override bool HasEverActivated()=>activated;public override PolicyEnvelope? Load()=>null;}
 sealed class TempDir:IDisposable{public string Path{get;}=System.IO.Path.Combine(System.IO.Path.GetTempPath(),"ymc-"+Guid.NewGuid());public TempDir()=>Directory.CreateDirectory(Path);public void Dispose()=>Directory.Delete(Path,true);}
}