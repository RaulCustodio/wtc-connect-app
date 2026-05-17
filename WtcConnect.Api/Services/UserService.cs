using MongoDB.Driver;
using WtcConnect.Api.Models;

namespace WtcConnect.Api.Services;

public class UserService
{
    private readonly IMongoCollection<User> _users;

    public UserService(IMongoClient client, IConfiguration config)
    {
        var database = client.GetDatabase(
            config["MongoDbSettings:DatabaseName"]
        );

        _users = database.GetCollection<User>("users");
    }

    public async Task CreateAsync(User user)
    {
        await _users.InsertOneAsync(user);
    }

    public async Task<User?> GetByEmailAsync(string email)
    {
        return await _users
            .Find(u => u.Email == email)
            .FirstOrDefaultAsync();
    }

    public async Task UpdatePasswordHashAsync(string userId, string passwordHash)
    {
        await _users.UpdateOneAsync(
            user => user.Id == userId,
            Builders<User>.Update
                .Set(user => user.PasswordHash, passwordHash)
                .Set(user => user.Password, null));
    }
}