# WTC Connect App

WTC Connect é um cliente Android de mensagens integrado ao CRM corporativo. Suporta conversas 1:1 e por grupo/segmento, push + popup in-app, histórico de mensagens e mensagens interativas (botões / links) para campanhas e atendimento.

## Requisitos
- Windows 10/11
- Android Studio Narwhal Feature Drop \| 2025.1.2 Patch 2 (recomendado)
- JDK 17 (o JDK empacotado no Android Studio é aceitável)
- Android SDK (instale via Android Studio)
- Git
- .NET SDK 8
- Docker Desktop com Docker Compose ou uma instância local do MongoDB

## Clonar repositório
git clone https://github.com/CyntiaHagers/wtc-connect-app.git
cd wtc-connect-app

## Estrutura principal do projeto
- app/ — módulo da aplicação Android
- app/src/main/java/br/com/fiap/wtcconnect/... — fontes Kotlin
- WtcConnect.Api/ — backend ASP.NET Core 8
- docker-compose.yml — MongoDB local para desenvolvimento e avaliação

## Firebase
O app ainda possui dependências Firebase para analytics e notificações, mas `app/google-services.json` não é necessário para o fluxo principal de avaliação local com API própria. O professor pode clonar, compilar e testar autenticação, chat, grupos, campanhas, mídia e deeplinks sem configurar Firebase.

## Bootstrap reproduzível

1. Suba o MongoDB local:

```powershell
docker compose up -d
```

2. Suba a API:

```powershell
dotnet run --project .\WtcConnect.Api\WtcConnect.Api.csproj
```

3. Instale o app no emulador Android:

```powershell
.\gradlew.bat installDebug
```

4. Abra o app no emulador. O build cai por padrão em `http://10.0.2.2:5281/`, então o app conversa com a API local sem precisar de `local.properties`.

## Como construir e rodar
- Abra o projeto no Android Studio e aguarde o Gradle sync.
- Build no Android Studio: Build > Make Project
- Pelo terminal (Windows):

.\gradlew.bat assembleDebug
.\gradlew.bat installDebug

- Ou use o Run do Android Studio para executar em emulador/dispositivo.

## API local no app Android
Por padrão, o app usa `http://10.0.2.2:5281/`, que funciona no emulador Android para acessar a API rodando no computador. `local.properties` é opcional e só deve ser usado quando você quiser sobrescrever esse endereço.

Para testar em um celular físico na mesma rede Wi-Fi:
- suba a API com o perfil de rede local:
  `dotnet run --project WtcConnect.Api --launch-profile lan-http`
- descubra o IP do computador na rede, por exemplo `192.168.0.10`
- compile/instale o app informando esse IP:
  `.\gradlew.bat installDebug -PapiBaseUrl=http://192.168.0.10:5281/`

## Testes
- Unit tests:
  .\gradlew.bat testDebugUnitTest

- Build da API:
  dotnet build .\WtcConnect.Api\WtcConnect.Api.csproj -c Release

- Instrumentation (dispositivo/emulador):
  .\gradlew.bat connectedAndroidTest
