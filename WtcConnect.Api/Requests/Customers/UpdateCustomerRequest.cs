using MongoDB.Bson;
using WtcConnect.Api.Models;

namespace WtcConnect.Api.Requests.Customers;

public class UpdateCustomerRequest
{
    public required string Id { get; set; }
    public string? SegmentId { get; set; }
    public string? Name { get; set; }
    public string? Phone { get; set; }
    public string? Address { get; set; }

    public Customer ToCustomer(Customer existing) => new()
    {
        Id = existing.Id,
        UserId = existing.UserId,
        SegmentId = SegmentId ?? existing.SegmentId,
        Name = Name ?? existing.Name,
        Phone = Phone ?? existing.Phone,
        Address = Address ?? existing.Address,
        CreatedAt = existing.CreatedAt,
        LastActionAt = DateTime.UtcNow
    };
}
