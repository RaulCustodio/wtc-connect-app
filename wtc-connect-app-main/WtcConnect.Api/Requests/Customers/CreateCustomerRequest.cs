using MongoDB.Bson;
using WtcConnect.Api.Models;

namespace WtcConnect.Api.Requests.Customers;

public class CreateCustomerRequest
{
    public required string UserId { get; set; }
    public string? SegmentId { get; set; }
    public required string Name { get; set; }
    public required string Phone { get; set; } = string.Empty;
    public required string Address { get; set; } = string.Empty;

    public Customer ToCustomer() => new()
    {
        Id = ObjectId.GenerateNewId().ToString(),
        UserId = UserId,
        SegmentId = SegmentId,
        Name = Name,
        Phone = Phone,
        Address = Address,
        CreatedAt = DateTime.UtcNow,
        LastActionAt = DateTime.UtcNow
    };
}
