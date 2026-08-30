@echo off
echo Iniciando geracao de App Bundle...

echo.
echo ============================================================
echo Gerando Bundle Release
echo ============================================================
call gradlew.bat :app:bundleRelease
if errorlevel 1 (
    echo [ERRO] Falha ao gerar bundle
    pause
    exit /b 1
)

echo.
echo ============================================================
echo Concluido! O bundle foi gerado.
echo ============================================================
pause
