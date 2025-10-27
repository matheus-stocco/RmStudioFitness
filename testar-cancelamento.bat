@echo off
echo ========================================
echo    TESTE DE CANCELAMENTO PAGHIPER
echo ========================================
echo.

echo 1. Verificando configuração atual...
echo.

echo Token configurado: %PAGHIPER_TOKEN%
if "%PAGHIPER_TOKEN%"=="" (
    echo ❌ Token não configurado
    echo.
    echo Para configurar o token, execute:
    echo set PAGHIPER_TOKEN=seu_token_real_aqui
    echo.
    echo Ou configure no application.properties:
    echo paghiper.api.token=seu_token_real_aqui
    echo.
) else (
    echo ✅ Token configurado: %PAGHIPER_TOKEN%
    echo.
)

echo 2. Iniciando aplicação...
echo.
echo A aplicação será iniciada. Teste o cancelamento de um plano
echo e verifique os logs no console para ver se o cancelamento
echo está funcionando na PagHiper.
echo.

echo Pressione qualquer tecla para continuar...
pause > nul

echo.
echo 3. Iniciando Spring Boot...
echo.
mvn spring-boot:run









