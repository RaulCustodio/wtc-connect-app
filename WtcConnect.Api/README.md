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
- Acesso à internet para alcançar o MongoDB Atlas configurado no projeto
- Docker Desktop com Docker Compose ou MongoDB local em `mongodb://localhost:27017` apenas se você quiser rodar sem Atlas

## Configuração

Por padrão, o projeto usa o MongoDB Atlas configurado em `appsettings.json`.

Para desenvolvimento e avaliação local, o projeto também já cai por padrão em:

- JWT local de desenvolvimento, se `Jwt:Key` não estiver configurada
- banco `wtc_connect`

O Mongo local em `mongodb://localhost:27017` é apenas um fallback opcional quando `MongoDbSettings:ConnectionString` não está configurada ou foi sobrescrita com placeholder no ambiente de desenvolvimento.

Se quiser sobrescrever esses valores, configure as variáveis abaixo no PowerShell:

```powershell
$env:Jwt__Key = "defina-uma-chave-jwt-forte-com-32-ou-mais-caracteres"
$env:MongoDbSettings__ConnectionString = "mongodb://localhost:27017"
$env:MongoDbSettings__DatabaseName = "wtc_connect"
```

Opcionalmente, você pode definir os mesmos valores via User Secrets ou no perfil de execução local.

## Bootstrap rápido

Fluxo padrão com Atlas:

```powershell
dotnet run --project .\WtcConnect.Api\WtcConnect.Api.csproj
```

Fluxo alternativo com Mongo local: se você tiver Docker Desktop instalado, suba o MongoDB com:

```powershell
docker compose up -d
```

Depois sobrescreva a connection string para o banco local:

```powershell
$env:MongoDbSettings__ConnectionString = "mongodb://localhost:27017"
$env:MongoDbSettings__DatabaseName = "wtc_connect"
dotnet run --project .\WtcConnect.Api\WtcConnect.Api.csproj
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

## Publicar no Render

O repositório já inclui [render.yaml](c:/Users/raulc/source/repos/wtc-connect-app/render.yaml) e [Dockerfile](c:/Users/raulc/source/repos/wtc-connect-app/WtcConnect.Api/Dockerfile) para criar um Web Service Docker no Render.

### Passo a passo

1. Envie as alterações para o GitHub.
2. No Render, clique em `New +` > `Web Service`.
3. Selecione este repositório.
4. Como o seu painel não oferece runtime `.NET`, mantenha `Language = Docker`.
5. Preencha os campos assim:

```text
Name: wtc-connect-api
Branch: main
Root Directory: (deixe vazio)
Docker Build Context Directory: .
Dockerfile Path: ./WtcConnect.Api/Dockerfile
Docker Command: (deixe vazio)
Pre-Deploy Command: (deixe vazio)
```

6. No painel do serviço, configure as variáveis sensíveis:

```text
ASPNETCORE_ENVIRONMENT=Production
Jwt__Key=<uma-chave-forte-com-32-ou-mais-caracteres>
MongoDbSettings__ConnectionString=<sua-string-do-mongodb-atlas>
MongoDbSettings__DatabaseName=wtc_connect
```

7. Faça o deploy.
8. Quando o Render fornecer a URL pública, valide:

```text
https://SEU-SERVICO.onrender.com/
https://SEU-SERVICO.onrender.com/chat
```

### Observações para produção

- O Render termina o HTTPS no proxy; a API já está preparada para `X-Forwarded-Proto`.
- O SignalR continua no endpoint `/chat` e funciona no Render via WebSockets.
- Se o MongoDB Atlas tiver whitelist por IP, libere o acesso do Render ao cluster.
- Não deixe a connection string do Atlas como segredo versionado para produção pública.

## Gerar APK apontando para a API publicada

Depois de publicar a API no Render, gere o app apontando para a URL pública:

```powershell
.\gradlew.bat assembleRelease -PapiBaseUrl=https://SEU-SERVICO.onrender.com/ -PsignalrHubUrl=https://SEU-SERVICO.onrender.com/chat
```

Para um teste rápido antes do APK final:

```powershell
.\gradlew.bat assembleDebug -PapiBaseUrl=https://SEU-SERVICO.onrender.com/ -PsignalrHubUrl=https://SEU-SERVICO.onrender.com/chat
```

## Observabilidade e auditoria

- Logs HTTP de entrada e saída com `traceId`
- Auditoria estruturada de login, cadastro, envio de mensagem, campanha e grupos
- Senhas armazenadas com hash PBKDF2

## Integração com o app Android

O app consome esta API via Retrofit e usa o hub `/chat` para mensagens em tempo real. Para emulador Android, o endereço padrão do app é `http://10.0.2.2:5281/`.

O professor pode validar o fluxo local com o setup padrão Atlas usando:

```powershell
dotnet run --project .\WtcConnect.Api\WtcConnect.Api.csproj
.\gradlew.bat installDebug
```

## Referências de apoio

- [../docs/sprint2-adequacao.md](../docs/sprint2-adequacao.md)
- [../docs/sprint2-backend-especificacao.md](../docs/sprint2-backend-especificacao.md)