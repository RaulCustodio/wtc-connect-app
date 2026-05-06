namespace WtcConnect.Api.Models;

public class CreateCampaignRequest
{
    public string Name { get; set; } = string.Empty;
    public string Content { get; set; } = string.Empty;
    public List<string> TargetCustomerIds { get; set; } = [];
}
