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
[Route("campaigns")]
public class CampaignsController : ControllerBase
{
    private readonly CampaignService _campaignService;
    private readonly MessageService _messageService;
    private readonly IHubContext<ChatHub> _hubContext;

    public CampaignsController(
        CampaignService campaignService,
        MessageService messageService,
        IHubContext<ChatHub> hubContext)
    {
        _campaignService = campaignService;
        _messageService = messageService;
        _hubContext = hubContext;
    }

    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var campaigns = await _campaignService.GetAllAsync();
        return Ok(campaigns);
    }

    [HttpPost]
    public async Task<IActionResult> Create([FromBody] CreateCampaignRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Name) || string.IsNullOrWhiteSpace(request.Content))
        {
            return BadRequest(new { message = "Name e content são obrigatórios" });
        }

        var customerIds = request.TargetCustomerIds
            .Where(customerId => !string.IsNullOrWhiteSpace(customerId))
            .Distinct()
            .ToList();

        if (customerIds.Count == 0)
        {
            return BadRequest(new { message = "Informe ao menos um customerId" });
        }

        var senderId = User.FindFirst("userId")?.Value;

        if (string.IsNullOrWhiteSpace(senderId))
        {
            return Unauthorized(new { message = "Token inválido" });
        }

        var senderRole = User.FindFirst(ClaimTypes.Role)?.Value ?? "Client";
        var now = DateTime.UtcNow;

        var campaign = new Campaign
        {
            Name = request.Name,
            Content = request.Content,
            TargetCustomerIds = customerIds,
            CreatedBy = senderId,
            Status = CampaignStatus.Sent,
            CreatedAt = now,
            SentAt = now
        };

        await _campaignService.CreateAsync(campaign);

        foreach (var customerId in customerIds)
        {
            var message = new Message
            {
                CustomerId = customerId,
                SenderId = senderId,
                SenderRole = senderRole,
                Content = request.Content,
                CampaignId = campaign.Id,
                Status = MessageStatus.Sent,
                CreatedAt = now
            };

            await _messageService.CreateAsync(message);

            await _hubContext.Clients
                .Group(ChatHub.GetCustomerGroup(customerId))
                .SendAsync("messageReceived", message);
        }

        return Ok(new
        {
            campaign,
            messagesSent = customerIds.Count
        });
    }
}
