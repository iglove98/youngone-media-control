namespace YoungOne.MediaControl.Agent.Policy;
public sealed record PolicyEnvelope(Guid PolicyId,long PolicyVersion,string PayloadJson,string PayloadHash,string Signature,string SigningKeyId,string PublicKey,DateTimeOffset EffectiveAt,DateTimeOffset? ExpiresAt);
public enum MediaAction{Allow,ReadOnly,Block}public enum OfflineBehavior{KeepLastValid,FailClosed,SafeAllowlistOnly}
