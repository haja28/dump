@echo off
REM Makan For You - Startup Script for Windows

setlocal enabledelayedexpansion

REM Colors
set "GREEN=[92m"
set "YELLOW=[93m"
set "RED=[91m"
set "CYAN=[96m"
set "RESET=[0m"

cls
echo.
echo %CYAN%===========================================%RESET%
echo %CYAN%Makan For You - Service Startup Script%RESET%
echo %CYAN%===========================================%RESET%
echo.

REM Check if we're in the right directory
if not exist "pom.xml" (
    echo %RED%ERROR: pom.xml not found!%RESET%
    echo %YELLOW%Please run this script from the project root directory%RESET%
    pause
    exit /b 1
)

echo %YELLOW%This script will start all services in separate windows.%RESET%
echo %YELLOW%Press any key when all services have started...%RESET%
echo.

REM Check Java
echo %CYAN%[1] Checking Java installation...%RESET%
java -version >nul 2>&1
if errorlevel 1 (
    echo %RED%ERROR: Java not found!%RESET%
    echo %YELLOW%Please install Java 17 or higher%RESET%
    pause
    exit /b 1
) else (
    echo %GREEN%✓ Java is installed%RESET%
)

REM Check Maven
echo %CYAN%[2] Checking Maven installation...%RESET%
mvn -version >nul 2>&1
if errorlevel 1 (
    echo %RED%ERROR: Maven not found!%RESET%
    echo %YELLOW%Please install Maven 3.8 or higher%RESET%
    pause
    exit /b 1
) else (
    echo %GREEN%✓ Maven is installed%RESET%
)

REM Build project
echo.
echo %CYAN%[3] Building project...%RESET%
mvn clean install -DskipTests -q
if errorlevel 1 (
    echo %RED%Build failed!%RESET%
    pause
    exit /b 1
) else (
    echo %GREEN%✓ Build successful%RESET%
)

REM Start services
echo.
echo %CYAN%[4] Starting services...%RESET%
echo %YELLOW%Opening new windows for each service...%RESET%
echo.

REM Auth Service
echo %YELLOW%► Starting Auth Service (port 8081)...%RESET%
start "Auth Service" cmd /k "cd auth-service && mvn spring-boot:run"
timeout /t 3 /nobreak

REM Kitchen Service
echo %YELLOW%► Starting Kitchen Service (port 8082)...%RESET%
start "Kitchen Service" cmd /k "cd kitchen-service && mvn spring-boot:run"
timeout /t 3 /nobreak

REM Menu Service
echo %YELLOW%► Starting Menu Service (port 8083)...%RESET%
start "Menu Service" cmd /k "cd menu-service && mvn spring-boot:run"
timeout /t 3 /nobreak

REM Order Service
echo %YELLOW%► Starting Order Service (port 8084)...%RESET%
start "Order Service" cmd /k "cd order-service && mvn spring-boot:run"
timeout /t 3 /nobreak

REM API Gateway
echo %YELLOW%► Starting API Gateway (port 8080)...%RESET%
start "API Gateway" cmd /k "cd api-gateway && mvn spring-boot:run"
timeout /t 3 /nobreak

echo.
echo %GREEN%✓ All services started!%RESET%
echo.
echo %CYAN%Services are running on:%RESET%
echo %GREEN%  ✓ API Gateway: http://localhost:8080%RESET%
echo %GREEN%  ✓ Auth Service: http://localhost:8081%RESET%
echo %GREEN%  ✓ Kitchen Service: http://localhost:8082%RESET%
echo %GREEN%  ✓ Menu Service: http://localhost:8083%RESET%
echo %GREEN%  ✓ Order Service: http://localhost:8084%RESET%
echo.

echo %CYAN%Test commands:%RESET%
echo %YELLOW%  curl http://localhost:8080/api/v1/kitchens%RESET%
echo %YELLOW%  curl http://localhost:8080/api/v1/menu-items%RESET%
echo %YELLOW%  curl http://localhost:8080/api/v1/orders%RESET%
echo.

echo %CYAN%Run diagnostics:%RESET%
echo %YELLOW%  powershell -ExecutionPolicy Bypass -File diagnose.ps1%RESET%
echo.

echo %YELLOW%Each service is running in its own window. Close any window to stop that service.%RESET%
echo %YELLOW%Press any key to close this window...%RESET%
pause
