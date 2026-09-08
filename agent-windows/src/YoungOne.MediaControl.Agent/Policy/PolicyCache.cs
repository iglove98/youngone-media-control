using System.Security.Cryptography;
using System.Text;
using System.Text.Json;
using Microsoft.Extensions.Options;
using Microsoft.Win32;
using NSec.Cryptography;
namespace YoungOne.MediaControl.Agent.Policy;
public class PolicyCache{
 const string RegistryPath=@"SOFTWARE\YoungOne\MediaControl\Agent";static readonly byte[] Entropy=Encoding.UTF8.GetBytes("YoungOne.MediaControl.PolicyCache.v1");
 readonly string path;readonly string activationPath;readonly byte[]? pinnedKey;
 public PolicyCache(IOptions<AgentOptions> options){path=Path.Combine(options.Value.DataDirectory,"policy.cache");activationPath=Path.Combine(options.Value.DataDirectory,"activation.latch");pinnedKey=string.IsNullOrWhiteSpace(options.Value.PinnedPolicyPublicKey)?null:ExtractRawEd25519(Convert.FromBase64String(options.Value.PinnedPolicyPublicKey));}
 public void Install(PolicyEnvelope envelope){if(pinnedKey is null)throw new SecurityException("Pinned policy public key is not configured");byte[] payload=Encoding.UTF8.GetBytes(envelope.PayloadJson);string hash=Convert.ToHexString(SHA256.HashData(payload)).ToLowerInvariant();if(!CryptographicOperations.FixedTimeEquals(Encoding.ASCII.GetBytes(hash),Encoding.ASCII.GetBytes(envelope.PayloadHash)))throw new SecurityException("Policy hash mismatch");PublicKey publicKey=PublicKey.Import(SignatureAlgorithm.Ed25519,pinnedKey,KeyBlobFormat.RawPublicKey);if(!SignatureAlgorithm.Ed25519.Verify(publicKey,payload,Convert.FromBase64String(envelope.Signature)))throw new SecurityException("Policy signature mismatch");Directory.CreateDirectory(Path.GetDirectoryName(path)!);WriteProtected(path,JsonSerializer.SerializeToUtf8Bytes(envelope));MarkActivated(envelope);}
 public virtual PolicyEnvelope? Load(){if(!File.Exists(path))return null;byte[] plain=ProtectedData.Unprotect(File.ReadAllBytes(path),Entropy,DataProtectionScope.LocalMachine);return JsonSerializer.Deserialize<PolicyEnvelope>(plain)??throw new SecurityException("Invalid cached policy");}
 public virtual bool HasEverActivated(){bool fileActivated=false;if(File.Exists(activationPath)){byte[] plain=ProtectedData.Unprotect(File.ReadAllBytes(activationPath),Entropy,DataProtectionScope.LocalMachine);ActivationLatch latch=JsonSerializer.Deserialize<ActivationLatch>(plain)??throw new SecurityException("Invalid activation latch");fileActivated=latch.Activated;}using RegistryKey? key=Registry.LocalMachine.OpenSubKey(RegistryPath);bool registryActivated=key?.GetValue("ControlActivated",0) is int value&&value==1;return fileActivated||registryActivated||File.Exists(path);}
 void MarkActivated(PolicyEnvelope envelope){var latch=new ActivationLatch(true,envelope.PolicyId,envelope.PolicyVersion,DateTimeOffset.UtcNow);WriteProtected(activationPath,JsonSerializer.SerializeToUtf8Bytes(latch));using RegistryKey key=Registry.LocalMachine.CreateSubKey(RegistryPath,true);key.SetValue("ControlActivated",1,RegistryValueKind.DWord);key.SetValue("ActivatedPolicyId",envelope.PolicyId.ToString("D"),RegistryValueKind.String);key.SetValue("ActivatedPolicyVersion",envelope.PolicyVersion,RegistryValueKind.QWord);}
 static void WriteProtected(string destination,byte[] plain){byte[] encrypted=ProtectedData.Protect(plain,Entropy,DataProtectionScope.LocalMachine);string temp=destination+".tmp";File.WriteAllBytes(temp,encrypted);File.Move(temp,destination,true);}
 static byte[] ExtractRawEd25519(byte[] spki){if(spki.Length<32)throw new SecurityException("Invalid Ed25519 public key");return spki[^32..];}
 sealed record ActivationLatch(bool Activated,Guid PolicyId,long PolicyVersion,DateTimeOffset ActivatedAt);
}