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

    public AuthController(JwtService jwtService, UserService userService)
    {
        _jwtService = jwtService;
        _userService = userService;
    }

    // 🔹 REGISTER
    [HttpPost("register")]
    [AllowAnonymous]
    public async Task<IActionResult> Register([FromBody] RegisterRequest request)
    {
        var existing = await _userService.GetByEmailAsync(request.Email);

        if (existing != null)
            return BadRequest(new { message = "Usuário já existe" });

        var user = new User
        {
            Email = request.Email,
            Password = request.Password,
            Role = string.IsNullOrWhiteSpace(request.Role) ? "Client" : request.Role
        };

        await _userService.CreateAsync(user);

        var token = _jwtService.GenerateToken(user);

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

        if (user == null || user.Password != request.Password)
        {
            return Unauthorized(new { message = "Credenciais inválidas" });
        }

        var token = _jwtService.GenerateToken(user);

        return Ok(new AuthResponse
        {
            Token = token,
            UserId = user.Id ?? string.Empty,
            Role = user.Role,
            Email = user.Email
        });
    }
}