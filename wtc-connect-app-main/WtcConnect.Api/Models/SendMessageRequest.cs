namespace WtcConnect.Api.Models;

public class SendMessageRequest
{
    public string CustomerId { get; set; } = string.Empty;
    public string Content { get; set; } = string.Empty;
    public string? CampaignId { get; set; }
}
