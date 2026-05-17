using WtcConnect.Api.Services;
using WtcConnect.Api.Models;
using WtcConnect.Api.Hubs;
using MongoDB.Driver;

using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.DataProtection;
using Microsoft.IdentityModel.Tokens;
using System.Text;
using System.Text.Json.Serialization;


var builder = WebApplication.CreateBuilder(args);

var baseConfiguration = new ConfigurationBuilder()
    .SetBasePath(builder.Environment.ContentRootPath)
    .AddJsonFile("appsettings.json", optional: true)
    .Build();

builder.Logging.ClearProviders();
builder.Logging.AddConsole();

var jwtKey = builder.Configuration["Jwt:Key"];
if (string.IsNullOrWhiteSpace(jwtKey) || jwtKey.Contains("CHANGE_ME", StringComparison.OrdinalIgnoreCase))
{
    if (builder.Environment.IsDevelopment())
    {
        jwtKey = "wtc-connect-development-jwt-key-2026-local-only";
        builder.Configuration["Jwt:Key"] = jwtKey;
        Console.WriteLine(
            "[Startup] Jwt:Key não foi configurada. Usando chave local de desenvolvimento. Configure Jwt__Key para ambientes compartilhados.");
    }
    else
    {
        throw new InvalidOperationException("Configure Jwt:Key via appsettings local ou variável de ambiente antes de iniciar a API.");
    }
}

var mongoConnectionString = builder.Configuration["MongoDbSettings:ConnectionString"];
if (string.IsNullOrWhiteSpace(mongoConnectionString) || mongoConnectionString.Contains("CHANGE_ME", StringComparison.OrdinalIgnoreCase))
{
    mongoConnectionString = baseConfiguration["MongoDbSettings:ConnectionString"];
}

if (string.IsNullOrWhiteSpace(mongoConnectionString) || mongoConnectionString.Contains("CHANGE_ME", StringComparison.OrdinalIgnoreCase))
{
    if (builder.Environment.IsDevelopment())
    {
        mongoConnectionString = "mongodb://localhost:27017";
        Console.WriteLine(
            "[Startup] MongoDbSettings:ConnectionString não foi configurada. Usando Mongo local em mongodb://localhost:27017 para desenvolvimento.");
    }
}

if (string.IsNullOrWhiteSpace(mongoConnectionString) || mongoConnectionString.Contains("CHANGE_ME", StringComparison.OrdinalIgnoreCase))
{
    throw new InvalidOperationException("Configure MongoDbSettings:ConnectionString via appsettings local ou variável de ambiente antes de iniciar a API.");
}

var mongoDatabaseName = builder.Configuration["MongoDbSettings:DatabaseName"];
if (string.IsNullOrWhiteSpace(mongoDatabaseName) || mongoDatabaseName.Contains("CHANGE_ME", StringComparison.OrdinalIgnoreCase))
{
    mongoDatabaseName = baseConfiguration["MongoDbSettings:DatabaseName"];
}

if (string.IsNullOrWhiteSpace(mongoDatabaseName))
{
    mongoDatabaseName = "wtc_connect";
}

builder.Configuration["MongoDbSettings:ConnectionString"] = mongoConnectionString;
builder.Configuration["MongoDbSettings:DatabaseName"] = mongoDatabaseName;

// Config Mongo (appsettings.json)
builder.Services.Configure<MongoDbSettings>(
    builder.Configuration.GetSection("MongoDbSettings"));

// Cliente Mongo
builder.Services.AddSingleton<IMongoClient>(sp =>
{
    var settings = builder.Configuration
        .GetSection("MongoDbSettings")
        .Get<MongoDbSettings>()!;

    return new MongoClient(settings.ConnectionString);
});

// Services
builder.Services.AddSingleton<JwtService>();
builder.Services.AddSingleton<UserService>();
builder.Services.AddSingleton<PasswordHasherService>();
builder.Services.AddSingleton<AuditService>();
builder.Services.AddSingleton<MessageService>();
builder.Services.AddSingleton<CampaignService>();
builder.Services.AddSingleton<CustomerService>();
builder.Services.AddSingleton<SegmentService>();
builder.Services.AddSingleton<GroupService>();
builder.Services.AddSignalR();

// CONFIG JWT 
var key = Encoding.UTF8.GetBytes(jwtKey);

builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.Events = new JwtBearerEvents
    {
        OnMessageReceived = context =>
        {
            var accessToken = context.Request.Query["access_token"];
            var path = context.HttpContext.Request.Path;

            if (!string.IsNullOrWhiteSpace(accessToken) && path.StartsWithSegments("/chat"))
            {
                context.Token = accessToken;
            }

            return Task.CompletedTask;
        }
    };

    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuer = false,
        ValidateAudience = false,
        ValidateIssuerSigningKey = true,
        IssuerSigningKey = new SymmetricSecurityKey(key)
    };
});

builder.Services.AddAuthorization();

builder.Services.AddDataProtection()
    .PersistKeysToFileSystem(new DirectoryInfo(
        Path.Combine(builder.Environment.ContentRootPath, ".aspnet-data-protection-keys")));

// Controllers + Swagger
builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.Converters.Add(new JsonStringEnumConverter());
    });
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.AddSecurityDefinition("Bearer", new Microsoft.OpenApi.Models.OpenApiSecurityScheme
    {
        Name = "Authorization",
        Type = Microsoft.OpenApi.Models.SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT",
        In = Microsoft.OpenApi.Models.ParameterLocation.Header,
        Description = "Enter 'Bearer {token}'"
    });

    c.AddSecurityRequirement(new Microsoft.OpenApi.Models.OpenApiSecurityRequirement
    {
        {
            new Microsoft.OpenApi.Models.OpenApiSecurityScheme
            {
                Reference = new Microsoft.OpenApi.Models.OpenApiReference
                {
                    Type = Microsoft.OpenApi.Models.ReferenceType.SecurityScheme,
                    Id = "Bearer"
                }
            },
            new string[] {}
        }
    });
});

var app = builder.Build();

app.Use(async (context, next) =>
{
    var logger = context.RequestServices.GetRequiredService<ILoggerFactory>().CreateLogger("RequestAudit");
    logger.LogInformation("HTTP {Method} {Path} traceId={TraceId}", context.Request.Method, context.Request.Path, context.TraceIdentifier);
    await next();
    logger.LogInformation("HTTP {Method} {Path} status={StatusCode} traceId={TraceId}", context.Request.Method, context.Request.Path, context.Response.StatusCode, context.TraceIdentifier);
});

// Swagger (SEMPRE ATIVO)
app.UseSwagger();
app.UseSwaggerUI(c =>
{
    c.SwaggerEndpoint("/swagger/v1/swagger.json", "WTC Connect API");
    c.RoutePrefix = "";
});

// 🔹 Middlewares
if (!app.Environment.IsDevelopment())
{
    app.UseHttpsRedirection();
}


app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();
app.MapHub<ChatHub>("/chat");

app.Run();
