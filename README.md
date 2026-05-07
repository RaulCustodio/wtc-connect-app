# WTC Connect App

WTC Connect é um cliente Android de mensagens integrado ao CRM corporativo. Suporta conversas 1:1 e por grupo/segmento, push + popup in-app, histórico de mensagens e mensagens interativas (botões / links) para campanhas e atendimento.

## Requisitos
- Windows 10/11
- Android Studio Narwhal Feature Drop \| 2025.1.2 Patch 2 (recomendado)
- JDK 17 (o JDK empacotado no Android Studio é aceitável)
- Android SDK (instale via Android Studio)
- Git
- Conexão com a internet (dependências remotas, Firebase)

## Clonar repositório
git clone https://github.com/CyntiaHagers/wtc-connect-app.git
cd wtc-connect-app

## Estrutura principal do projeto
- app/ — módulo da aplicação Android
- app/src/main/java/br/com/fiap/wtcconnect/... — fontes Kotlin
- app/google-services.json — configuração Firebase esperada para br.com.fiap.wtcconnect

## Firebase
O projeto usa app/google-services.json. Verifique se o arquivo existe e corresponde ao seu projeto Firebase. Se alterar o package name, substitua app/google-services.json pelo JSON correto do Firebase Console.

## Como construir e rodar
- Abra o projeto no Android Studio e aguarde o Gradle sync.
- Build no Android Studio: Build > Make Project
- Pelo terminal (Windows):

.\gradlew.bat assembleDebug
.\gradlew.bat installDebug

- Ou use o Run do Android Studio para executar em emulador/dispositivo.

## Testes
- Unit tests:
  .\gradlew.bat testDebugUnitTest

- Instrumentation (dispositivo/emulador):
  .\gradlew.bat connectedAndroidTest
