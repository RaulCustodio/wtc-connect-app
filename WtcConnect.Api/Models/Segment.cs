using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace WtcConnect.Api.Models;
public class Segment
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public required string Id { get; set; }

    public required string Name { get; set; }
}

