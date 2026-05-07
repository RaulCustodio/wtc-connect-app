using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.SignalR;

namespace WtcConnect.Api.Hubs;

[Authorize]
public class ChatHub : Hub
{
    public static string GetCustomerGroup(string customerId) => $"customer:{customerId}";

    public override async Task OnConnectedAsync()
    {
        var userId = Context.User?.FindFirst("userId")?.Value;

        if (!string.IsNullOrWhiteSpace(userId))
        {
            await Groups.AddToGroupAsync(Context.ConnectionId, GetCustomerGroup(userId));
        }

        await base.OnConnectedAsync();
    }

    public async Task JoinCustomerInbox(string customerId)
    {
        if (string.IsNullOrWhiteSpace(customerId))
        {
            return;
        }

        await Groups.AddToGroupAsync(Context.ConnectionId, GetCustomerGroup(customerId));
    }

    public async Task LeaveCustomerInbox(string customerId)
    {
        if (string.IsNullOrWhiteSpace(customerId))
        {
            return;
        }

        await Groups.RemoveFromGroupAsync(Context.ConnectionId, GetCustomerGroup(customerId));
    }
}
