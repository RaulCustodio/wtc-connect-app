using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using WtcConnect.Api.Models;
using WtcConnect.Api.Services;

namespace WtcConnect.Api.Controllers;

[ApiController]
[Route("auth")]
public class AuthController : ControllerBase
{
    private readonly JwtService _jwtService;
    private readonly UserService _userService;
    private readonly PasswordHasherService _passwordHasher;
    private readonly AuditService _auditService;

    public AuthController(
        JwtService jwtService,
        UserService userService,
        PasswordHasherService passwordHasher,
        AuditService auditService)
    {
        _jwtService = jwtService;
        _userService = userService;
        _passwordHasher = passwordHasher;
        _auditService = auditService;
    }

    // 🔹 REGISTER
    [HttpPost("register")]
    [AllowAnonymous]
    public async Task<IActionResult> Register([FromBody] RegisterRequest request)
    {
        var existing = await _userService.GetByEmailAsync(request.Email);

        if (existing != null)
        {
            _auditService.Log(HttpContext, "auth.register", "denied", request.Email, "email_already_exists");
            return BadRequest(new { message = "Usuário já existe" });
        }

        var user = new User
        {
            Email = request.Email,
            PasswordHash = _passwordHasher.HashPassword(request.Password),
            Role = string.IsNullOrWhiteSpace(request.Role) ? "Client" : request.Role
        };

        await _userService.CreateAsync(user);

        var token = _jwtService.GenerateToken(user);
        _auditService.Log(HttpContext, "auth.register", "success", user.Email, user.Role);

        return Ok(new AuthResponse
        {
            Token = token,
            UserId = user.Id ?? string.Empty,
            Email = user.Email,
            Role = user.Role
        });
    }

    // 🔹 LOGIN
    [HttpPost("login")]
    [AllowAnonymous]
    public async Task<IActionResult> Login([FromBody] LoginRequest request)
    {
        var user = await _userService.GetByEmailAsync(request.Email);

        if (user == null)
        {
            _auditService.Log(HttpContext, "auth.login", "denied", request.Email, "user_not_found");
            return Unauthorized(new { message = "Credenciais inválidas" });
        }

        var passwordMatches = !string.IsNullOrWhiteSpace(user.PasswordHash)
            ? _passwordHasher.VerifyPassword(request.Password, user.PasswordHash)
            : string.Equals(user.Password, request.Password, StringComparison.Ordinal);

        if (!passwordMatches)
        {
            _auditService.Log(HttpContext, "auth.login", "denied", request.Email, "invalid_password");
            return Unauthorized(new { message = "Credenciais inválidas" });
        }

        if (string.IsNullOrWhiteSpace(user.PasswordHash))
        {
            user.PasswordHash = _passwordHasher.HashPassword(request.Password);
            await _userService.UpdatePasswordHashAsync(user.Id ?? string.Empty, user.PasswordHash);
        }

        var token = _jwtService.GenerateToken(user);
        _auditService.Log(HttpContext, "auth.login", "success", user.Email, user.Role);

        return Ok(new AuthResponse
        {
            Token = token,
            UserId = user.Id ?? string.Empty,
            Role = user.Role,
            Email = user.Email
        });
    }
}