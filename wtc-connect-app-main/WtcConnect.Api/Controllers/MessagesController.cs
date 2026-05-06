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

    public MessagesController(MessageService messageService, IHubContext<ChatHub> hubContext)
    {
        _messageService = messageService;
        _hubContext = hubContext;
    }

    [HttpPost]
    public async Task<IActionResult> Send([FromBody] SendMessageRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.CustomerId) || string.IsNullOrWhiteSpace(request.Content))
        {
            return BadRequest(new { message = "CustomerId e content são obrigatórios" });
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
            CampaignId = request.CampaignId,
            Status = MessageStatus.Sent,
            CreatedAt = DateTime.UtcNow
        };

        await _messageService.CreateAsync(message);

        await _hubContext.Clients
            .Group(ChatHub.GetCustomerGroup(message.CustomerId))
            .SendAsync("messageReceived", message);

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

        return Ok(message);
    }
}
