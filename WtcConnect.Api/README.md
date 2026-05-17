# WTC Connect API

API ASP.NET Core 8 para autenticação, cadastro de clientes, segmentos, campanhas e mensageria em tempo real via SignalR.

## Stack

- ASP.NET Core 8
- MongoDB
- JWT Bearer Authentication
- SignalR
- Swagger/OpenAPI

## Estrutura

- `Controllers/`: endpoints REST
- `Services/`: regras de negócio, persistência MongoDB, JWT, auditoria e hash de senha
- `Models/`: documentos NoSQL e DTOs de domínio
- `Requests/`: payloads de entrada para customers e segments
- `Hubs/ChatHub.cs`: hub SignalR para atualização em tempo real

## Pré-requisitos

- .NET SDK 8
- Docker Desktop com Docker Compose ou MongoDB local em `mongodb://localhost:27017`

## Configuração

Para desenvolvimento e avaliação local, o projeto já cai por padrão em:

- JWT local de desenvolvimento, se `Jwt:Key` não estiver configurada
- MongoDB local em `mongodb://localhost:27017`
- banco `wtc_connect`

Se quiser sobrescrever esses valores, configure as variáveis abaixo no PowerShell:

```powershell
$env:Jwt__Key = "defina-uma-chave-jwt-forte-com-32-ou-mais-caracteres"
$env:MongoDbSettings__ConnectionString = "mongodb://localhost:27017"
$env:MongoDbSettings__DatabaseName = "wtc_connect"
```

Opcionalmente, você pode definir os mesmos valores via User Secrets ou no perfil de execução local.

## Bootstrap rápido

Se você tiver Docker Desktop instalado, suba o MongoDB com:

```powershell
docker compose up -d
```

## Executar

```powershell
dotnet run --project .\WtcConnect.Api\WtcConnect.Api.csproj
```

Após subir, a documentação Swagger fica disponível na raiz:

- `http://localhost:5281/`

O hub SignalR fica em:

- `http://localhost:5281/chat`

## Build

```powershell
dotnet build .\WtcConnect.Api\WtcConnect.Api.csproj -c Release
```

## Observabilidade e auditoria

- Logs HTTP de entrada e saída com `traceId`
- Auditoria estruturada de login, cadastro, envio de mensagem, campanha e grupos
- Senhas armazenadas com hash PBKDF2

## Integração com o app Android

O app consome esta API via Retrofit e usa o hub `/chat` para mensagens em tempo real. Para emulador Android, o endereço padrão do app é `http://10.0.2.2:5281/`.

O professor pode validar o fluxo local com:

```powershell
dotnet run --project .\WtcConnect.Api\WtcConnect.Api.csproj
.\gradlew.bat installDebug
```

## Referências de apoio

- [../docs/sprint2-adequacao.md](../docs/sprint2-adequacao.md)
- [../docs/sprint2-backend-especificacao.md](../docs/sprint2-backend-especificacao.md)