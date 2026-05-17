using System.Security.Claims;

namespace WtcConnect.Api.Services;

public class AuditService
{
    private readonly ILogger<AuditService> _logger;

    public AuditService(ILogger<AuditService> logger)
    {
        _logger = logger;
    }

    public void Log(HttpContext? httpContext, string action, string outcome, string? target = null, string? detail = null)
    {
        var userId = httpContext?.User?.FindFirst("userId")?.Value ?? "anonymous";
        var email = httpContext?.User?.FindFirst(ClaimTypes.Name)?.Value ?? "anonymous";
        var remoteIp = httpContext?.Connection.RemoteIpAddress?.ToString() ?? "unknown";
        var traceId = httpContext?.TraceIdentifier ?? "n/a";

        _logger.LogInformation(
            "Audit action={Action} outcome={Outcome} userId={UserId} email={Email} target={Target} detail={Detail} remoteIp={RemoteIp} traceId={TraceId}",
            action,
            outcome,
            userId,
            email,
            target ?? "n/a",
            detail ?? "n/a",
            remoteIp,
            traceId);
    }
}