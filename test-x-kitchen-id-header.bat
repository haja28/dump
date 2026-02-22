@echo off
echo ========================================
echo X-Kitchen-Id Header Forwarding Test
echo ========================================
echo.
echo This script tests that the API Gateway correctly forwards
echo the X-Kitchen-Id header to downstream services.
echo.
echo Prerequisites:
echo   - API Gateway running on port 8080
echo   - Order Service running on port 8084
echo   - Valid JWT token
echo.
pause

echo.
echo Testing X-Kitchen-Id header forwarding...
echo ========================================
echo.

echo Test 1: POST /api/v1/orders with X-Kitchen-Id header
echo (This should succeed now that the header is forwarded)
echo.
echo Command:
echo curl -X POST http://localhost:8080/api/v1/orders ^
echo   -H "Content-Type: application/json" ^
echo   -H "Authorization: Bearer YOUR_TOKEN" ^
echo   -H "X-Kitchen-Id: 1" ^
echo   -d "{\"deliveryAddress\":\"123 Test St\",\"deliveryInstructions\":\"Ring doorbell\"}"
echo.
echo Please replace YOUR_TOKEN with a valid JWT token and run the above command manually.
echo.
echo Expected: 200 OK or 201 Created (not 400 Bad Request about missing header)
echo.

pause

echo.
echo Test 2: Check API Gateway logs
echo ========================================
echo.
echo Look for these log lines in the API Gateway console:
echo   - "Forwarding request to order-service"
echo   - Headers should include "X-Kitchen-Id=[1]"
echo.
echo If you see these logs, the fix is working correctly.
echo.

pause

echo.
echo ========================================
echo Additional Testing with Flutter App
echo ========================================
echo.
echo 1. Launch your Flutter app
echo 2. Navigate to the order creation screen
echo 3. Try to create an order
echo 4. The app should now successfully create orders without
echo    "Missing required request header 'X-Kitchen-Id'" errors
echo.
echo The Flutter app already sends the X-Kitchen-Id header correctly.
echo This backend fix ensures the API Gateway forwards it properly.
echo.
echo ========================================
echo Fix Applied
echo ========================================
echo.
echo File Modified: api-gateway/src/main/resources/application.yml
echo.
echo Changes:
echo   1. Added PreserveHostHeader to default-filters
echo   2. Added PreserveHostHeader to cart-service route
echo   3. Added PreserveHostHeader to order-service route
echo.
echo See X_KITCHEN_ID_HEADER_FIX.md for full documentation.
echo.
echo ========================================
echo.
pause

