using System.Security;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json;
using NSec.Cryptography;
using YoungOne.MediaControl.Agent.Security;
using Xunit;
namespace YoungOne.MediaControl.Agent.Tests;
public sealed class UninstallTokenVerifierTests{
 [Fact]public void VerifyAcceptsSignedTokenBoundToAgentAndInstallation(){using Key key=CreateKey();Guid agentId=Guid.NewGuid();DateTimeOffset now=DateTimeOffset.UtcNow;var envelope=CreateEnvelope(key,agentId,"install-a",now.AddMinutes(-1),now.AddMinutes(15));var token=UninstallTokenVerifier.Verify(envelope,ExportPublicKey(key),agentId,"INSTALL-A",now);Assert.Equal(agentId,token.AgentId);Assert.Equal("install-a",token.InstallationId);}
 [Fact]public void VerifyRejectsTokenForDifferentAgent(){using Key key=CreateKey();DateTimeOffset now=DateTimeOffset.UtcNow;var envelope=CreateEnvelope(key,Guid.NewGuid(),"install-a",now,now.AddMinutes(15));Assert.Throws<SecurityException>(()=>UninstallTokenVerifier.Verify(envelope,ExportPublicKey(key),Guid.NewGuid(),"install-a",now));}
 [Fact]public void VerifyRejectsExpiredToken(){using Key key=CreateKey();Guid agentId=Guid.NewGuid();DateTimeOffset now=DateTimeOffset.UtcNow;var envelope=CreateEnvelope(key,agentId,"install-a",now.AddMinutes(-20),now.AddMinutes(-1));Assert.Throws<SecurityException>(()=>UninstallTokenVerifier.Verify(envelope,ExportPublicKey(key),agentId,"install-a",now));}
 [Fact]public void VerifyRejectsTamperedPayload(){using Key key=CreateKey();Guid agentId=Guid.NewGuid();DateTimeOffset now=DateTimeOffset.UtcNow;var envelope=CreateEnvelope(key,agentId,"install-a",now,now.AddMinutes(15));envelope=envelope with{PayloadJson=envelope.PayloadJson.Replace("approved","tampered",StringComparison.Ordinal)};Assert.Throws<SecurityException>(()=>UninstallTokenVerifier.Verify(envelope,ExportPublicKey(key),agentId,"install-a",now));}
 static Key CreateKey()=>Key.Create(SignatureAlgorithm.Ed25519,new KeyCreationParameters{ExportPolicy=KeyExportPolicies.AllowPlaintextExport});
 static string ExportPublicKey(Key key)=>Convert.ToBase64String(key.PublicKey.Export(KeyBlobFormat.RawPublicKey));
 static UninstallTokenEnvelope CreateEnvelope(Key key,Guid agentId,string installationId,DateTimeOffset issuedAt,DateTimeOffset expiresAt){var payload=new UninstallTokenPayload(1,agentId,installationId,Guid.NewGuid().ToString("N"),issuedAt,expiresAt,"approved","admin-test");string json=JsonSerializer.Serialize(payload);byte[] bytes=Encoding.UTF8.GetBytes(json);return new(json,Convert.ToHexString(SHA256.HashData(bytes)).ToLowerInvariant(),Convert.ToBase64String(SignatureAlgorithm.Ed25519.Sign(key,bytes)),"test-key");}
}