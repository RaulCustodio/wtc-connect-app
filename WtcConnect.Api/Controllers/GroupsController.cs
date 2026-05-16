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
[Route("groups")]
public class GroupsController : ControllerBase
{
    private readonly GroupService _groupService;
    private readonly MessageService _messageService;
    private readonly UserService _userService;
    private readonly IHubContext<ChatHub> _hubContext;

    public GroupsController(
        GroupService groupService,
        MessageService messageService,
        UserService userService,
        IHubContext<ChatHub> hubContext)
    {
        _groupService = groupService;
        _messageService = messageService;
        _userService = userService;
        _hubContext = hubContext;
    }

    [AllowAnonymous]
    [HttpGet("status")]
    public IActionResult Status()
    {
        return Ok(new { status = "API running" });
    }

    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var groups = await _groupService.GetAllAsync();
        return Ok(groups);
    }

    [HttpPost]
    public async Task<IActionResult> Create([FromBody] CreateGroupRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Name))
        {
            return BadRequest(new { message = "Name é obrigatório" });
        }

        var group = await _groupService.CreateAsync(request.Name.Trim());
        return Ok(group);
    }

    [HttpGet("users/search")]
    public async Task<IActionResult> SearchUsers([FromQuery] string query = "", [FromQuery] string? groupId = null)
    {
        var users = await _groupService.SearchUsersAsync(query, groupId);
        return Ok(users);
    }

    [HttpGet("users/{userId}/group")]
    public async Task<IActionResult> GetUserGroup(string userId)
    {
        var groupId = await _groupService.GetUserGroupIdAsync(userId);
        return Ok(new { groupId });
    }

    [HttpGet("users/{userId}")]
    public async Task<IActionResult> GetUser(string userId)
    {
        var member = await _groupService.GetUserAsync(userId);

        if (member is not null)
        {
            return Ok(member);
        }

        return NotFound(new { message = "Usuário não encontrado em grupos" });
    }

    [HttpGet("{groupId}/members")]
    public async Task<IActionResult> GetMembers(string groupId)
    {
        var group = await _groupService.GetByIdAsync(groupId);

        if (group is null)
        {
            return NotFound(new { message = "Grupo não encontrado" });
        }

        var members = await _groupService.GetMembersAsync(groupId);
        return Ok(members);
    }

    [HttpPost("{groupId}/members")]
    public async Task<IActionResult> AddMember(string groupId, [FromBody] AddGroupMemberRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Email))
        {
            return BadRequest(new { message = "Email é obrigatório" });
        }

        var group = await _groupService.GetByIdAsync(groupId);

        if (group is null)
        {
            return NotFound(new { message = "Grupo não encontrado" });
        }

        var member = await _groupService.AddMemberByEmailAsync(groupId, request.Email);
        return Ok(member);
    }

    [HttpDelete("{groupId}/members/{userId}")]
    public async Task<IActionResult> RemoveMember(string groupId, string userId)
    {
        var removed = await _groupService.RemoveMemberAsync(groupId, userId);

        if (removed is null)
        {
            return NotFound(new { message = "Usuário não pertence a este grupo" });
        }

        return Ok(removed);
    }

    [HttpGet("{groupId}/messages")]
    public async Task<IActionResult> GetMessages(string groupId)
    {
        var group = await _groupService.GetByIdAsync(groupId);

        if (group is null)
        {
            return NotFound(new { message = "Grupo não encontrado" });
        }

        var messages = await _messageService.GetInboxByCustomerIdAsync(ChatHub.GetGroupConversationId(groupId));
        return Ok(messages);
    }

    [HttpPost("{groupId}/messages")]
    public async Task<IActionResult> SendMessage(string groupId, [FromBody] SendGroupMessageRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Content))
        {
            return BadRequest(new { message = "Content é obrigatório" });
        }

        var group = await _groupService.GetByIdAsync(groupId);

        if (group is null)
        {
            return NotFound(new { message = "Grupo não encontrado" });
        }

        var senderId = User.FindFirst("userId")?.Value;

        if (string.IsNullOrWhiteSpace(senderId))
        {
            return Unauthorized(new { message = "Token inválido" });
        }

        var senderEmail = User.FindFirst(ClaimTypes.Name)?.Value;

        if (!string.IsNullOrWhiteSpace(senderEmail))
        {
            var user = await _userService.GetByEmailAsync(senderEmail);

            if (user is not null)
            {
                await _groupService.EnsureMemberAsync(groupId, user);
            }
        }

        var message = new Message
        {
            CustomerId = ChatHub.GetGroupConversationId(groupId),
            SenderId = senderId,
            SenderRole = User.FindFirst(ClaimTypes.Role)?.Value ?? "Client",
            Content = request.Content,
            Status = MessageStatus.Sent,
            CreatedAt = DateTime.UtcNow
        };

        await _messageService.CreateAsync(message);

        await _hubContext.Clients
            .Group(ChatHub.GetSignalRGroup(groupId))
            .SendAsync("messageReceived", message);

        return Ok(message);
    }
}

public class SendGroupMessageRequest
{
    public string Content { get; set; } = string.Empty;
}
