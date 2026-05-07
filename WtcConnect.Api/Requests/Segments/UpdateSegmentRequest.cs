using MongoDB.Bson;
using WtcConnect.Api.Models;

namespace WtcConnect.Api.Requests.Segments
{
    public class UpdateSegmentRequest
    {
        public required string Id { get; set; }
        public string? Name { get; set; }

        public Segment ToSegment(Segment existing) => new()
        {
            Id = ObjectId.GenerateNewId().ToString(),
            Name = Name ?? existing.Name,
        };
    }
}
