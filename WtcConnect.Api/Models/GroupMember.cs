using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace WtcConnect.Api.Models;

public class GroupMember
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    public string GroupId { get; set; } = string.Empty;
    public string UserId { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public string? Email { get; set; }
    public DateTime AddedAt { get; set; } = DateTime.UtcNow;
}
