namespace YoungOne.MediaControl.Agent.Detection;

public enum MediaOperation { Connect, Open, Read, Write, Execute, Format, Eject }
public enum EnforcementDecision { Allow, ReadOnly, Block, DetectOnly }
public sealed record RiskEventRequest(
    string EventId,
    string PayloadHash,
    string? UserId,
    string? DeviceInstanceId,
    string? SerialHash,
    string MediaType,
    string Operation,
    string Decision,
    string ReasonCode,
    Guid? PolicyId,
    long? PolicyVersion,
    bool EnforcementApplied,
    string EnforcementResultCode,
    bool PopupShown,
    DateTimeOffset OccurredAt);
public sealed record RiskEventResponse(
    Guid RiskEventId,
    int RiskScore,
    string Severity,
    int AttemptCount24h,
    string Status,
    bool Duplicate,
    bool AppealRecommended);
