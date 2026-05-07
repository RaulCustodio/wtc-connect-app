using MongoDB.Driver;
using WtcConnect.Api.Models;

namespace WtcConnect.Api.Services;

public class CampaignService
{
    private readonly IMongoCollection<Campaign> _campaigns;

    public CampaignService(IMongoClient client, IConfiguration config)
    {
        var database = client.GetDatabase(config["MongoDbSettings:DatabaseName"]);
        _campaigns = database.GetCollection<Campaign>("campaigns");
    }

    public async Task<Campaign> CreateAsync(Campaign campaign)
    {
        await _campaigns.InsertOneAsync(campaign);
        return campaign;
    }

    public async Task<List<Campaign>> GetAllAsync()
    {
        return await _campaigns
            .Find(_ => true)
            .SortByDescending(campaign => campaign.CreatedAt)
            .ToListAsync();
    }
}
