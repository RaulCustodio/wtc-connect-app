using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using WtcConnect.Api.Services;

namespace WtcConnect.Api.Controllers;

[ApiController]
[Authorize]
[Route("inbox")]
public class InboxController : ControllerBase
{
    private readonly MessageService _messageService;

    public InboxController(MessageService messageService)
    {
        _messageService = messageService;
    }

    [HttpGet("{customerId}")]
    public async Task<IActionResult> GetByCustomerId(string customerId)
    {
        var messages = await _messageService.GetInboxByCustomerIdAsync(customerId);
        return Ok(messages);
    }
}
