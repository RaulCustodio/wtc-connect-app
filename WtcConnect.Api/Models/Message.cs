using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace WtcConnect.Api.Models;

public enum MessageStatus
{
    Sent,
    Delivered,
    Read
}

public class Message
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    public string CustomerId { get; set; } = string.Empty;
    public string SenderId { get; set; } = string.Empty;
    public string SenderRole { get; set; } = string.Empty;
    public string Content { get; set; } = string.Empty;

    [BsonRepresentation(BsonType.String)]
    public MessageStatus Status { get; set; } = MessageStatus.Sent;

    public string? CampaignId { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? DeliveredAt { get; set; }
    public DateTime? ReadAt { get; set; }
}
