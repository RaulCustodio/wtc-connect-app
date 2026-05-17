using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace WtcConnect.Api.Models;

[BsonIgnoreExtraElements]
public class User
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    public string Email { get; set; } = string.Empty;
    public string PasswordHash { get; set; } = string.Empty;
    public string? Password { get; set; }

    public string Role { get; set; } = "Client"; // padrão
}