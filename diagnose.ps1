# Makan For You - Diagnostic Script for Windows PowerShell

# Color helper
function Write-Success { Write-Host $args[0] -ForegroundColor Green }
function Write-Error { Write-Host $args[0] -ForegroundColor Red }
function Write-Warning { Write-Host $args[0] -ForegroundColor Yellow }
function Write-Info { Write-Host $args[0] -ForegroundColor Cyan }

# Clear screen
Clear-Host
Write-Info "=========================================="
Write-Info "Makan For You - API Gateway Diagnostics"
Write-Info "=========================================="

# Check if ports are in use
Write-Info "`n[1] Checking which services are running..."
Write-Warning "Service Port Check:"

$ports = @{
    "API Gateway" = 8080
    "Auth Service" = 8081
    "Kitchen Service" = 8082
    "Menu Service" = 8083
    "Order Service" = 8084
}

$runningServices = @()

foreach ($service in $ports.Keys) {
    $port = $ports[$service]
    $connection = Test-NetConnection -ComputerName localhost -Port $port -WarningAction SilentlyContinue

    if ($connection.TcpTestSucceeded) {
        Write-Success "✓ $service is RUNNING (port $port)"
        $runningServices += $service
    } else {
        Write-Error "✗ $service is NOT running (port $port)"
    }
}

# Test health endpoints
Write-Info "`n[2] Testing health endpoints..."

function Test-Endpoint {
    param(
        [string]$Url,
        [string]$ServiceName
    )

    try {
        $response = Invoke-WebRequest -Uri $Url -TimeoutSec 5 -ErrorAction Stop
        Write-Success "✓ $ServiceName responded: HTTP $($response.StatusCode)"
        return $true
    } catch {
        Write-Error "✗ $ServiceName failed: $($_.Exception.Message)"
        return $false
    }
}

$healthTests = @{
    "http://localhost:8080/health" = "API Gateway"
    "http://localhost:8081/health" = "Auth Service"
    "http://localhost:8082/health" = "Kitchen Service"
    "http://localhost:8083/health" = "Menu Service"
    "http://localhost:8084/health" = "Order Service"
}

$healthyServices = @()

foreach ($url in $healthTests.Keys) {
    if (Test-Endpoint -Url $url -ServiceName $healthTests[$url]) {
        $healthyServices += $healthTests[$url]
    }
}

# Test Kitchen endpoint
Write-Info "`n[3] Testing Kitchen endpoint..."

Write-Warning "`nDirect Kitchen Service:"
Test-Endpoint -Url "http://localhost:8082/api/v1/kitchens" -ServiceName "Kitchen Service (/api/v1/kitchens)"

Write-Warning "`nThrough API Gateway:"
Test-Endpoint -Url "http://localhost:8080/api/v1/kitchens" -ServiceName "API Gateway (/api/v1/kitchens)"

# Check MySQL
Write-Info "`n[4] Checking MySQL..."

try {
    $mysqlStatus = Get-Service MySQL80 -ErrorAction SilentlyContinue
    if ($null -ne $mysqlStatus) {
        if ($mysqlStatus.Status -eq "Running") {
            Write-Success "✓ MySQL service is RUNNING"
        } else {
            Write-Error "✗ MySQL service is STOPPED"
        }
    } else {
        Write-Warning "? Could not find MySQL80 service"
    }
} catch {
    Write-Warning "? MySQL check skipped: $_"
}

# Summary
Write-Info "`n=========================================="
Write-Info "SUMMARY"
Write-Info "=========================================="

Write-Info "`nServices Running: $($runningServices.Count)/5"
foreach ($service in $runningServices) {
    Write-Success "  ✓ $service"
}

Write-Info "`nServices Responding: $($healthyServices.Count)/5"
foreach ($service in $healthyServices) {
    Write-Success "  ✓ $service"
}

# Recommendations
Write-Info "`n=========================================="
Write-Info "RECOMMENDATIONS"
Write-Info "=========================================="

if ($runningServices.Count -eq 0) {
    Write-Warning "`n⚠ No services are running!"
    Write-Info "Start services in separate PowerShell terminals:"
    Write-Info "  1. cd kitchen-service; mvn spring-boot:run"
    Write-Info "  2. cd auth-service; mvn spring-boot:run"
    Write-Info "  3. cd menu-service; mvn spring-boot:run"
    Write-Info "  4. cd order-service; mvn spring-boot:run"
    Write-Info "  5. cd api-gateway; mvn spring-boot:run"
} elseif ($runningServices.Count -lt 5) {
    Write-Warning "`n⚠ Not all services are running!"
    Write-Info "Missing services:"
    foreach ($service in $ports.Keys) {
        if ($runningServices -notcontains $service) {
            Write-Info "  - $service"
        }
    }
} elseif ($healthyServices.Count -eq 5) {
    Write-Success "`n✓ All services are healthy!"
    Write-Info "`nYou can now test endpoints:"
    Write-Info "  GET http://localhost:8080/api/v1/kitchens"
    Write-Info "  GET http://localhost:8080/api/v1/menu-items"
    Write-Info "  GET http://localhost:8080/api/v1/orders"
} else {
    Write-Warning "`n⚠ Some services are running but not responding!"
    Write-Info "Check the service logs for errors."
}

Write-Info "`n=========================================="
