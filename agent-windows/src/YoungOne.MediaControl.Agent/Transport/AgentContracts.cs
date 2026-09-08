namespace YoungOne.MediaControl.Agent.Transport;
public sealed record RegisterRequest(string BootstrapToken,string InstallationId,string Hostname,string AgentVersion,string OsVersion,string EnvironmentType,string DeviceEvidenceHash,string BootSessionId);
public sealed record RegisterResponse(Guid AgentId,string AgentKey,string SecurityStatus,string IdentityDisposition);
public sealed record HeartbeatRequest(string EventId,string AgentVersion,string OsVersion,long QueueDepth,string? PolicyVersion,string ControlStatus);
