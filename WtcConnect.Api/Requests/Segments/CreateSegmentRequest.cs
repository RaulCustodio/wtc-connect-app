using MongoDB.Bson;
using WtcConnect.Api.Models;

namespace WtcConnect.Api.Requests.Segments
{
    public class CreateSegmentRequest
    {
        public required string Name { get; set; }

        public Segment ToSegment() => new ()
        {
            Id = ObjectId.GenerateNewId().ToString(),
            Name = Name
        };
    }
}
