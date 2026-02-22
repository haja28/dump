@echo off
echo ========================================
echo CART ROUTING FIX - API GATEWAY RESTART
echo ========================================
echo.
echo This script will:
echo 1. Rebuild the API Gateway with new cart routing
echo 2. Instructions to restart the API Gateway
echo.
echo NOTE: You need to manually stop the API Gateway first!
echo       Press Ctrl+C in the API Gateway terminal window
echo.
pause

cd /d "%~dp0"

echo.
echo Step 1: Cleaning and rebuilding API Gateway...
echo ========================================
call mvn clean install -pl api-gateway -am -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Build failed!
    echo Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo ========================================
echo BUILD SUCCESSFUL!
echo ========================================
echo.
echo Next Steps:
echo 1. If API Gateway is still running, stop it (Ctrl+C in its terminal)
echo 2. Run this command to start API Gateway:
echo    cd api-gateway
echo    mvn spring-boot:run
echo.
echo Or use the main startup.bat script to start all services
echo.
echo ========================================
echo Testing the Fix:
echo ========================================
echo.
echo After API Gateway is restarted, test with:
echo   curl -X GET http://localhost:8080/api/v1/cart -H "X-User-Id: 1"
echo.
echo   curl -X POST http://localhost:8080/api/v1/cart/items -H "X-User-Id: 1" -H "Content-Type: application/json" -d "{\"menuItemId\": 1, \"quantity\": 2}"
echo.
pause
