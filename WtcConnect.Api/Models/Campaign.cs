using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace WtcConnect.Api.Models;

public enum CampaignStatus
{
    Draft,
    Sent
}

public class Campaign
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    public string Name { get; set; } = string.Empty;
    public string Content { get; set; } = string.Empty;
    public List<string> TargetCustomerIds { get; set; } = [];
    public string CreatedBy { get; set; } = string.Empty;

    [BsonRepresentation(BsonType.String)]
    public CampaignStatus Status { get; set; } = CampaignStatus.Sent;

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? SentAt { get; set; }
}
