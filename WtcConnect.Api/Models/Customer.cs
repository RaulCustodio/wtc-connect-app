using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace WtcConnect.Api.Models;

public class Customer
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    public required string UserId { get; set; }
    public string? SegmentId { get; set; }
    public required string Name { get; set; }
    public required string Phone { get; set; } = string.Empty;
    public required string Address { get; set; } = string.Empty;
    public required DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public required DateTime LastActionAt { get; set; } = DateTime.UtcNow;
}