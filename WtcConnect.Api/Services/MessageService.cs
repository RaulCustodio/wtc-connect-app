using MongoDB.Driver;
using WtcConnect.Api.Models;

namespace WtcConnect.Api.Services;

public class MessageService
{
    private readonly IMongoCollection<Message> _messages;

    public MessageService(IMongoClient client, IConfiguration config)
    {
        var database = client.GetDatabase(config["MongoDbSettings:DatabaseName"]);
        _messages = database.GetCollection<Message>("messages");
    }

    public async Task<Message> CreateAsync(Message message)
    {
        await _messages.InsertOneAsync(message);
        return message;
    }

    public async Task<List<Message>> GetInboxByCustomerIdAsync(string customerId)
    {
        return await _messages
            .Find(message => message.CustomerId == customerId)
            .SortByDescending(message => message.CreatedAt)
            .ToListAsync();
    }

    public async Task<Message?> UpdateStatusAsync(string id, MessageStatus status)
    {
        var updates = new List<UpdateDefinition<Message>>
        {
            Builders<Message>.Update.Set(message => message.Status, status)
        };

        if (status == MessageStatus.Delivered)
        {
            updates.Add(Builders<Message>.Update.Set(message => message.DeliveredAt, DateTime.UtcNow));
        }

        if (status == MessageStatus.Read)
        {
            var now = DateTime.UtcNow;
            updates.Add(Builders<Message>.Update.Set(message => message.DeliveredAt, now));
            updates.Add(Builders<Message>.Update.Set(message => message.ReadAt, now));
        }

        return await _messages.FindOneAndUpdateAsync(
            message => message.Id == id,
            Builders<Message>.Update.Combine(updates),
            new FindOneAndUpdateOptions<Message>
            {
                ReturnDocument = ReturnDocument.After
            });
    }
}
