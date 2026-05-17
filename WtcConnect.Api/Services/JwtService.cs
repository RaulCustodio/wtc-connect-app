using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.IdentityModel.Tokens;
using WtcConnect.Api.Models;

namespace WtcConnect.Api.Services;

public class JwtService
{
    private readonly byte[] _key;

    public JwtService(IConfiguration configuration)
    {
        var secretKey = configuration["Jwt:Key"];

        if (string.IsNullOrWhiteSpace(secretKey) || secretKey.Contains("CHANGE_ME", StringComparison.OrdinalIgnoreCase))
        {
            throw new InvalidOperationException("Configure Jwt:Key via appsettings local ou variável de ambiente antes de iniciar a API.");
        }

        _key = Encoding.UTF8.GetBytes(secretKey);
    }

    public string GenerateToken(User user)
    {
        var claims = new[]
        {
            new Claim(ClaimTypes.Name, user.Email),
            new Claim(ClaimTypes.Role, user.Role),
            new Claim("userId", user.Id ?? "")
        };

        var credentials = new SigningCredentials(
            new SymmetricSecurityKey(_key),
            SecurityAlgorithms.HmacSha256
        );

        var token = new JwtSecurityToken(
            claims: claims,
            expires: DateTime.UtcNow.AddHours(2),
            signingCredentials: credentials
        );

        return new JwtSecurityTokenHandler().WriteToken(token);
    }
}