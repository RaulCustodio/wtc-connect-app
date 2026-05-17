using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using WtcConnect.Api.Hubs;
using WtcConnect.Api.Models;
using WtcConnect.Api.Services;

namespace WtcConnect.Api.Controllers;

[ApiController]
[Authorize]
[Route("messages")]
public class MessagesController : ControllerBase
{
    private readonly MessageService _messageService;
    private readonly IHubContext<ChatHub> _hubContext;
    private readonly AuditService _auditService;

    public MessagesController(
        MessageService messageService,
        IHubContext<ChatHub> hubContext,
        AuditService auditService)
    {
        _messageService = messageService;
        _hubContext = hubContext;
        _auditService = auditService;
    }

    [HttpPost]
    public async Task<IActionResult> Send([FromBody] SendMessageRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.CustomerId) ||
            (string.IsNullOrWhiteSpace(request.Content) && string.IsNullOrWhiteSpace(request.MediaUrl)))
        {
            return BadRequest(new { message = "CustomerId e pelo menos um conteúdo textual ou mídia são obrigatórios" });
        }

        var senderId = User.FindFirst("userId")?.Value;

        if (string.IsNullOrWhiteSpace(senderId))
        {
            return Unauthorized(new { message = "Token inválido" });
        }

        var message = new Message
        {
            CustomerId = request.CustomerId,
            SenderId = senderId,
            SenderRole = User.FindFirst(ClaimTypes.Role)?.Value ?? "Client",
            Content = request.Content,
            MediaUrl = request.MediaUrl,
            MediaType = request.MediaType,
            CampaignId = request.CampaignId,
            Status = MessageStatus.Sent,
            CreatedAt = DateTime.UtcNow
        };

        await _messageService.CreateAsync(message);

        await _hubContext.Clients
            .Group(ChatHub.GetCustomerGroup(message.CustomerId))
            .SendAsync("messageReceived", message);

        _auditService.Log(HttpContext, "messages.send", "success", message.CustomerId, message.Id);

        return Ok(message);
    }

    [HttpPatch("{id}/status")]
    public async Task<IActionResult> UpdateStatus(string id, [FromBody] UpdateMessageStatusRequest request)
    {
        var message = await _messageService.UpdateStatusAsync(id, request.Status);

        if (message is null)
        {
            return NotFound(new { message = "Mensagem não encontrada" });
        }

        await _hubContext.Clients
            .Group(ChatHub.GetCustomerGroup(message.CustomerId))
            .SendAsync("messageStatusUpdated", message);

        _auditService.Log(HttpContext, "messages.update_status", "success", message.CustomerId, $"{message.Id}:{message.Status}");

        return Ok(message);
    }
}
