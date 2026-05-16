using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace WtcConnect.Api.Models;

public class Group
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    public string Name { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}
