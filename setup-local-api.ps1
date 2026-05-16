# Script to auto-detect local machine IP and configure local.properties
# Run this before building: .\setup-local-api.ps1

# Get the local machine IP - prefer real network adapters
$ips = Get-NetIPAddress -AddressFamily IPv4 -Type Unicast |
    Where-Object {
        $_.InterfaceAlias -notlike "*Loopback*" -and
        $_.InterfaceAlias -notlike "*vEthernet*" -and
        $_.InterfaceAlias -notlike "*Docker*" -and
        $_.InterfaceAlias -notlike "*VMware*" -and
        $_.InterfaceAlias -notlike "*Bluetooth*" -and
        $_.IPAddress -notmatch "^169\." # Skip APIPA addresses
    }

# Prefer WiFi, Ethernet, then others
$ip = ($ips | Where-Object { $_.InterfaceAlias -match "Wi-Fi|Wireless|Ethernet" -and $_.InterfaceAlias -notlike "*Bluetooth*" } | Select-Object -First 1).IPAddress

# If not found, use any available private network IP
if (-not $ip) {
    $ip = ($ips | Where-Object { $_.IPAddress -match "^(192\.168|10\.)" } | Select-Object -First 1).IPAddress
}

# If still not found, use the first available
if (-not $ip) {
    $ip = ($ips | Select-Object -First 1).IPAddress
}

if (-not $ip) {
    Write-Host "❌ Erro: Não foi possível detectar o IP da máquina" -ForegroundColor Red
    exit 1
}

$localPropertiesPath = "$PSScriptRoot/local.properties"
$apiUrl = "http\://$ip\:5281/"
$signalrUrl = "http\://$ip\:5281/chat"

# Read or create local.properties
$content = @()
if (Test-Path $localPropertiesPath) {
    $content = Get-Content $localPropertiesPath | ForEach-Object {
        if ($_ -match "^api\.base\.url=") {
            "api.base.url=$apiUrl"
        } elseif ($_ -match "^signalr\.hub\.url=") {
            "signalr.hub.url=$signalrUrl"
        } else {
            $_
        }
    }
} else {
    $content = @(
        "## Local development configuration",
        "## This file is generated automatically and should NOT be committed",
        "",
        "api.base.url=$apiUrl",
        "signalr.hub.url=$signalrUrl",
        "sdk.dir=$env:ANDROID_HOME"
    )
}

# Ensure api.base.url and signalr.hub.url exist
if ($content -notmatch "^api\.base\.url=") {
    $content += "api.base.url=$apiUrl"
}
if ($content -notmatch "^signalr\.hub\.url=") {
    $content += "signalr.hub.url=$signalrUrl"
}

# Write back
$content | Set-Content $localPropertiesPath

Write-Host "✅ local.properties atualizado:" -ForegroundColor Green
Write-Host "   API URL: $apiUrl"
Write-Host "   SignalR URL: $signalrUrl"
Write-Host "   IP detectado: $ip"
