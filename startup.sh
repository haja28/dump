#!/bin/bash

# Makan For You - Startup Script for Linux/Mac

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Clear screen
clear

echo -e "${CYAN}=========================================="
echo -e "Makan For You - Service Startup Script"
echo -e "==========================================${NC}\n"

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo -e "${RED}ERROR: pom.xml not found!${NC}"
    echo -e "${YELLOW}Please run this script from the project root directory${NC}"
    exit 1
fi

# Check Java
echo -e "${CYAN}[1] Checking Java installation...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}ERROR: Java not found!${NC}"
    echo -e "${YELLOW}Please install Java 17 or higher${NC}"
    exit 1
else
    echo -e "${GREEN}✓ Java is installed${NC}"
    java -version
fi

# Check Maven
echo -e "\n${CYAN}[2] Checking Maven installation...${NC}"
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}ERROR: Maven not found!${NC}"
    echo -e "${YELLOW}Please install Maven 3.8 or higher${NC}"
    exit 1
else
    echo -e "${GREEN}✓ Maven is installed${NC}"
    mvn -version | head -n 3
fi

# Build project
echo -e "\n${CYAN}[3] Building project...${NC}"
mvn clean install -DskipTests -q
if [ $? -ne 0 ]; then
    echo -e "${RED}Build failed!${NC}"
    exit 1
else
    echo -e "${GREEN}✓ Build successful${NC}"
fi

# Start services
echo -e "\n${CYAN}[4] Starting services...${NC}"
echo -e "${YELLOW}Opening new terminals for each service...${NC}\n"

# Function to start a service
start_service() {
    local service=$1
    local port=$2

    echo -e "${YELLOW}► Starting $service (port $port)...${NC}"

    # For macOS
    if [[ "$OSTYPE" == "darwin"* ]]; then
        open -a Terminal "$(pwd)/$service"
        cd "$service"
        mvn spring-boot:run
        cd ..
    # For Linux
    else
        gnome-terminal -- bash -c "cd $service; mvn spring-boot:run; bash"
    fi

    sleep 3
}

# Start all services in background
start_service "auth-service" "8081" &
start_service "kitchen-service" "8082" &
start_service "menu-service" "8083" &
start_service "order-service" "8084" &
start_service "api-gateway" "8080" &

wait

echo -e "\n${GREEN}✓ All services started!${NC}\n"

echo -e "${CYAN}Services are running on:${NC}"
echo -e "${GREEN}  ✓ API Gateway: http://localhost:8080${NC}"
echo -e "${GREEN}  ✓ Auth Service: http://localhost:8081${NC}"
echo -e "${GREEN}  ✓ Kitchen Service: http://localhost:8082${NC}"
echo -e "${GREEN}  ✓ Menu Service: http://localhost:8083${NC}"
echo -e "${GREEN}  ✓ Order Service: http://localhost:8084${NC}\n"

echo -e "${CYAN}Test commands:${NC}"
echo -e "${YELLOW}  curl http://localhost:8080/api/v1/kitchens${NC}"
echo -e "${YELLOW}  curl http://localhost:8080/api/v1/menu-items${NC}"
echo -e "${YELLOW}  curl http://localhost:8080/api/v1/orders${NC}\n"

echo -e "${CYAN}Run diagnostics:${NC}"
echo -e "${YELLOW}  powershell -ExecutionPolicy Bypass -File diagnose.ps1${NC}\n"

echo -e "${YELLOW}Each service is running in its own terminal.${NC}"
echo -e "${YELLOW}Close any terminal to stop that service.${NC}"
