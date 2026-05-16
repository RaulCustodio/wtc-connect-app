using MongoDB.Driver;
using WtcConnect.Api.Models;

namespace WtcConnect.Api.Services;

public class GroupService
{
    private const string DefaultGroupName = "WTC Connect";
    private readonly IMongoCollection<Group> _groups;
    private readonly IMongoCollection<GroupMember> _members;
    private readonly IMongoCollection<User> _users;

    public GroupService(IMongoClient client, IConfiguration config)
    {
        var database = client.GetDatabase(config["MongoDbSettings:DatabaseName"]);
        _groups = database.GetCollection<Group>("groups");
        _members = database.GetCollection<GroupMember>("group_members");
        _users = database.GetCollection<User>("users");
    }

    public async Task<Group> EnsureDefaultGroupAsync()
    {
        var existing = await _groups.Find(g => g.Name == DefaultGroupName).FirstOrDefaultAsync();

        if (existing is not null)
        {
            return existing;
        }

        var group = new Group
        {
            Name = DefaultGroupName,
            CreatedAt = DateTime.UtcNow
        };

        await _groups.InsertOneAsync(group);
        return group;
    }

    public async Task<List<Group>> GetAllAsync()
    {
        await EnsureDefaultGroupAsync();
        return await _groups
            .Find(_ => true)
            .SortBy(g => g.Name)
            .ToListAsync();
    }

    public async Task<Group?> GetByIdAsync(string id)
    {
        return await _groups.Find(g => g.Id == id).FirstOrDefaultAsync();
    }

    public async Task<Group> CreateAsync(string name)
    {
        var existing = await _groups.Find(g => g.Name == name).FirstOrDefaultAsync();

        if (existing is not null)
        {
            return existing;
        }

        var group = new Group
        {
            Name = name,
            CreatedAt = DateTime.UtcNow
        };

        await _groups.InsertOneAsync(group);
        return group;
    }

    public async Task<List<GroupMember>> GetMembersAsync(string groupId)
    {
        return await _members
            .Find(m => m.GroupId == groupId)
            .SortBy(m => m.Name)
            .ToListAsync();
    }

    public async Task<GroupMember> AddMemberByEmailAsync(string groupId, string email)
    {
        var normalizedEmail = email.Trim().ToLowerInvariant();
        var emailFilter = Builders<User>.Filter.Regex(
            user => user.Email,
            new MongoDB.Bson.BsonRegularExpression(
                $"^{System.Text.RegularExpressions.Regex.Escape(normalizedEmail)}$",
                "i")
        );
        var user = await _users.Find(emailFilter).FirstOrDefaultAsync();
        var userId = user?.Id ?? normalizedEmail;
        var name = user?.Email?.Split('@')[0] ?? normalizedEmail.Split('@')[0];

        var existing = await _members
            .Find(m => m.GroupId == groupId && m.UserId == userId)
            .FirstOrDefaultAsync();

        if (existing is not null)
        {
            return existing;
        }

        var member = new GroupMember
        {
            GroupId = groupId,
            UserId = userId,
            Name = name,
            Email = normalizedEmail,
            AddedAt = DateTime.UtcNow
        };

        await _members.InsertOneAsync(member);
        return member;
    }

    public async Task<GroupMember> EnsureMemberAsync(string groupId, User user)
    {
        var member = await _members
            .Find(m => m.GroupId == groupId && m.UserId == user.Id)
            .FirstOrDefaultAsync();

        if (member is not null)
        {
            return member;
        }

        member = new GroupMember
        {
            GroupId = groupId,
            UserId = user.Id ?? string.Empty,
            Name = user.Email.Split('@')[0],
            Email = user.Email,
            AddedAt = DateTime.UtcNow
        };

        await _members.InsertOneAsync(member);
        return member;
    }

    public async Task<GroupMember?> RemoveMemberAsync(string groupId, string userId)
    {
        return await _members.FindOneAndDeleteAsync(m => m.GroupId == groupId && m.UserId == userId);
    }

    public async Task<string?> GetUserGroupIdAsync(string userId)
    {
        var member = await _members
            .Find(m => m.UserId == userId)
            .SortByDescending(m => m.AddedAt)
            .FirstOrDefaultAsync();

        if (member is not null)
        {
            return member.GroupId;
        }

        var defaultGroup = await EnsureDefaultGroupAsync();
        return defaultGroup.Id;
    }

    public async Task<GroupMember?> GetUserAsync(string userId)
    {
        return await _members
            .Find(m => m.UserId == userId)
            .SortByDescending(m => m.AddedAt)
            .FirstOrDefaultAsync();
    }

    public async Task<List<GroupMember>> SearchUsersAsync(string query, string? groupId)
    {
        var filter = Builders<GroupMember>.Filter.Empty;

        if (!string.IsNullOrWhiteSpace(groupId))
        {
            filter &= Builders<GroupMember>.Filter.Eq(m => m.GroupId, groupId);
        }

        if (!string.IsNullOrWhiteSpace(query))
        {
            var regex = new MongoDB.Bson.BsonRegularExpression(query, "i");
            filter &= Builders<GroupMember>.Filter.Or(
                Builders<GroupMember>.Filter.Regex(m => m.Name, regex),
                Builders<GroupMember>.Filter.Regex(m => m.Email, regex)
            );
        }

        return await _members
            .Find(filter)
            .SortBy(m => m.Name)
            .ToListAsync();
    }
}
