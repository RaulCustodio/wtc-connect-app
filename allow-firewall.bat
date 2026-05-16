@echo off
REM This script allows port 5281 through Windows Firewall for WTC Connect API
REM Right-click and select "Run as administrator"

echo Abrindo porta 5281 no Firewall do Windows...
netsh advfirewall firewall add rule name="WTC Connect API - Port 5281" dir=in action=allow protocol=tcp localport=5281 profile=any
netsh advfirewall firewall add rule name="WTC Connect API - Port 5281 OUT" dir=out action=allow protocol=tcp localport=5281 profile=any

echo.
echo Regras de firewall adicionadas com sucesso!
echo Porta 5281 agora esta aberta para conexoes locais.
pause
