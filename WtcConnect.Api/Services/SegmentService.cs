using MongoDB.Driver;
using WtcConnect.Api.Models;

namespace WtcConnect.Api.Services;

public class SegmentService
{
    private readonly IMongoCollection<Segment> segments;

    public SegmentService(IMongoClient client, IConfiguration config)
    {
        var database = client.GetDatabase(
            config["MongoDbSettings:DatabaseName"]
        );

        segments = database.GetCollection<Segment>("segments");
    }

    public async Task<Segment> CreateAsync(Segment segment)
    {
        await segments.InsertOneAsync(segment);
        return segment;
    }

    public async Task<Segment?> GetByIdAsync(string id)
    {
        return await segments.Find(s => s.Id == id).FirstOrDefaultAsync();
    }

    public async Task<Segment?> GetByNameAsync(string name)
    {
        return await segments.Find(s => s.Name == name).FirstOrDefaultAsync();
    }

    public async Task<Segment?> UpdateAsync(Segment segment)
    {
        await segments.ReplaceOneAsync(s => s.Id == segment.Id, segment);
        return segment;
    }

    public async Task<Segment?> DeleteAsync(string id)
    {
        var segment = await GetByIdAsync(id);

        if (segment is null) return null;

        await segments.DeleteOneAsync(s => s.Id == id);

        return segment;
    }
}

