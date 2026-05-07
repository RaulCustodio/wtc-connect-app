using MongoDB.Driver;
using WtcConnect.Api.Models;
using WtcConnect.Api.Requests.Customers;

namespace WtcConnect.Api.Services;

public class CustomerService
{
    private readonly IMongoCollection<Customer> customers;
    private readonly IMongoCollection<User> users;

    public CustomerService(IMongoClient client, IConfiguration config)
    {
        var database = client.GetDatabase(
            config["MongoDbSettings:DatabaseName"]
        );

        customers = database.GetCollection<Customer>("customers");
        users = database.GetCollection<User>("users");
    }

    public async Task<Customer> CreateAsync(Customer customer)
    {
        await customers.InsertOneAsync(customer);
        return customer;
    }

    public async Task<Customer?> GetByIdAsync(string id)
    {
        return await customers.Find(c => c.Id == id).FirstOrDefaultAsync();
    }

    public async Task<Customer?> GetByUserIdAsync(string id)
    {
        return await customers.Find(c => c.UserId == id).FirstOrDefaultAsync();
    }

    public async Task<Customer?> GetByUserEmailAsync(string email)
    {
        var user = users.Find(c => c.Email == email).FirstOrDefault();

        if (user is null) return null;

        return await customers.Find(c => c.UserId == user.Id).FirstOrDefaultAsync();
    }

    public async Task<List<Customer>> GetBySegmentAsync(string id)
    {
        return await customers.Find(c => c.SegmentId == id).ToListAsync();
    }

    public async Task<Customer?> UpdateAsync(Customer customer)
    {
        await customers.ReplaceOneAsync(c => c.Id == customer.Id, customer);

        return customer;
    }

    public async Task<Customer?> DeleteAsync(String id)
    {
        var customer = await GetByIdAsync(id);

        if(customer is null) return null;

        await customers.DeleteOneAsync(c => c.Id == id);

        return customer;
    }

    public async Task UpdateLastActionById(string id)
    {
        await customers.UpdateOneAsync(
            c => c.Id == id,
            Builders<Customer>.Update.Set(c => c.LastActionAt, DateTime.UtcNow)
        );
    }
}
