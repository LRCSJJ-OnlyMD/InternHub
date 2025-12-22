@echo off
REM InternHub - Azure Quick Deployment Script (Windows)
REM This script helps deploy InternHub to Azure Container Apps

setlocal enabledelayedexpansion

REM Configuration
set RESOURCE_GROUP=internhub-rg
set LOCATION=eastus
set ACR_NAME=internhubacr
set DB_SERVER_NAME=internhub-db-server
set REDIS_NAME=internhub-cache
set ENV_NAME=internhub-env

echo ========================================
echo   InternHub Azure Deployment Script
echo ========================================
echo.

REM Check if Azure CLI is installed
where az >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Error: Azure CLI is not installed
    echo Please install it from: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli
    pause
    exit /b 1
)

REM Check if logged in
echo Checking Azure login status...
az account show >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Not logged in. Opening Azure login...
    az login
)

echo [OK] Azure CLI logged in
echo.

REM List subscriptions
echo Available subscriptions:
az account list --output table
echo.

set /p SUBSCRIPTION_ID="Enter subscription ID (or press Enter to use default): "
if not "!SUBSCRIPTION_ID!"=="" (
    az account set --subscription "!SUBSCRIPTION_ID!"
    echo [OK] Subscription set
)

echo.
echo Using Resource Group: %RESOURCE_GROUP%
echo Using Location: %LOCATION%
echo.

set /p CONFIRM="Do you want to proceed? (y/n): "
if /i not "!CONFIRM!"=="y" (
    echo Deployment cancelled.
    pause
    exit /b 0
)

REM Create Resource Group
echo.
echo Step 1: Creating Resource Group...
az group create --name %RESOURCE_GROUP% --location %LOCATION%
echo [OK] Resource Group created

REM Create Container Apps Environment
echo.
echo Step 2: Creating Container Apps Environment...
az extension add --name containerapp --upgrade
az containerapp env create --name %ENV_NAME% --resource-group %RESOURCE_GROUP% --location %LOCATION%
echo [OK] Container Apps Environment created

REM Create PostgreSQL Database
echo.
echo Step 3: Creating PostgreSQL Database...
set /p DB_ADMIN_USER="Enter PostgreSQL admin username: "
set /p DB_ADMIN_PASSWORD="Enter PostgreSQL admin password: "

az postgres flexible-server create --name %DB_SERVER_NAME% --resource-group %RESOURCE_GROUP% --location %LOCATION% --admin-user !DB_ADMIN_USER! --admin-password "!DB_ADMIN_PASSWORD!" --sku-name Standard_B1ms --tier Burstable --storage-size 32 --version 16 --public-access 0.0.0.0-0.0.0.0

az postgres flexible-server db create --resource-group %RESOURCE_GROUP% --server-name %DB_SERVER_NAME% --database-name internhub

echo [OK] PostgreSQL Database created

REM Create Redis Cache
echo.
echo Step 4: Creating Redis Cache...
az redis create --name %REDIS_NAME% --resource-group %RESOURCE_GROUP% --location %LOCATION% --sku Basic --vm-size c0

for /f "usebackq tokens=*" %%i in (`az redis show --name %REDIS_NAME% --resource-group %RESOURCE_GROUP% --query hostName -o tsv`) do set REDIS_HOST=%%i
for /f "usebackq tokens=*" %%i in (`az redis list-keys --name %REDIS_NAME% --resource-group %RESOURCE_GROUP% --query primaryKey -o tsv`) do set REDIS_KEY=%%i

echo [OK] Redis Cache created

REM Create Container Registry
echo.
echo Step 5: Creating Container Registry...
az acr create --resource-group %RESOURCE_GROUP% --name %ACR_NAME% --sku Basic --admin-enabled true

for /f "usebackq tokens=*" %%i in (`az acr credential show --name %ACR_NAME% --resource-group %RESOURCE_GROUP% --query username -o tsv`) do set ACR_USERNAME=%%i
for /f "usebackq tokens=*" %%i in (`az acr credential show --name %ACR_NAME% --resource-group %RESOURCE_GROUP% --query passwords[0].value -o tsv`) do set ACR_PASSWORD=%%i

echo [OK] Container Registry created

REM Build and Push Docker Images
echo.
echo Step 6: Building and Pushing Docker Images...
az acr login --name %ACR_NAME%

echo Building backend...
docker build -t %ACR_NAME%.azurecr.io/internhub-backend:latest ./backend
docker push %ACR_NAME%.azurecr.io/internhub-backend:latest

echo Building frontend...
docker build -t %ACR_NAME%.azurecr.io/internhub-frontend:latest ./frontend
docker push %ACR_NAME%.azurecr.io/internhub-frontend:latest

echo [OK] Docker images pushed

REM Get JWT Secret
echo.
set /p JWT_SECRET="Enter JWT Secret (or press Enter to generate): "
if "!JWT_SECRET!"=="" (
    REM Generate random base64 string (simplified for Windows)
    set JWT_SECRET=PLEASE_GENERATE_A_SECURE_256_BIT_SECRET_KEY_USING_OPENSSL_OR_SIMILAR_TOOL
    echo Note: Please replace JWT_SECRET with a secure value later
)

REM Get Email Configuration
echo.
echo Email Configuration (for password reset):
set /p MAIL_HOST="Enter SMTP host (default: smtp.gmail.com): "
if "!MAIL_HOST!"=="" set MAIL_HOST=smtp.gmail.com
set /p MAIL_PORT="Enter SMTP port (default: 587): "
if "!MAIL_PORT!"=="" set MAIL_PORT=587
set /p MAIL_USERNAME="Enter email username: "
set /p MAIL_PASSWORD="Enter email password: "

REM Deploy Backend
echo.
echo Step 7: Deploying Backend Container App...
set DB_HOST=%DB_SERVER_NAME%.postgres.database.azure.com

az containerapp create --name internhub-backend --resource-group %RESOURCE_GROUP% --environment %ENV_NAME% --image %ACR_NAME%.azurecr.io/internhub-backend:latest --registry-server %ACR_NAME%.azurecr.io --registry-username !ACR_USERNAME! --registry-password !ACR_PASSWORD! --target-port 8080 --ingress external --min-replicas 1 --max-replicas 2 --cpu 1.0 --memory 2.0Gi --env-vars "SPRING_DATASOURCE_URL=jdbc:postgresql://!DB_HOST!:5432/internhub?sslmode=require" "SPRING_DATASOURCE_USERNAME=!DB_ADMIN_USER!" "SPRING_DATASOURCE_PASSWORD=!DB_ADMIN_PASSWORD!" "SPRING_DATA_REDIS_HOST=!REDIS_HOST!" "SPRING_DATA_REDIS_PASSWORD=!REDIS_KEY!" "SPRING_DATA_REDIS_PORT=6380" "SPRING_DATA_REDIS_SSL=true" "JWT_SECRET=!JWT_SECRET!" "SPRING_MAIL_HOST=!MAIL_HOST!" "SPRING_MAIL_PORT=!MAIL_PORT!" "SPRING_MAIL_USERNAME=!MAIL_USERNAME!" "SPRING_MAIL_PASSWORD=!MAIL_PASSWORD!"

for /f "usebackq tokens=*" %%i in (`az containerapp show --name internhub-backend --resource-group %RESOURCE_GROUP% --query properties.configuration.ingress.fqdn -o tsv`) do set BACKEND_URL=%%i
echo [OK] Backend deployed at: https://!BACKEND_URL!

REM Deploy Frontend
echo.
echo Step 8: Deploying Frontend Container App...

az containerapp create --name internhub-frontend --resource-group %RESOURCE_GROUP% --environment %ENV_NAME% --image %ACR_NAME%.azurecr.io/internhub-frontend:latest --registry-server %ACR_NAME%.azurecr.io --registry-username !ACR_USERNAME! --registry-password !ACR_PASSWORD! --target-port 80 --ingress external --min-replicas 1 --max-replicas 2 --cpu 0.5 --memory 1.0Gi --env-vars "API_URL=https://!BACKEND_URL!/api"

for /f "usebackq tokens=*" %%i in (`az containerapp show --name internhub-frontend --resource-group %RESOURCE_GROUP% --query properties.configuration.ingress.fqdn -o tsv`) do set FRONTEND_URL=%%i
echo [OK] Frontend deployed at: https://!FRONTEND_URL!

REM Update Backend with Frontend URL
echo.
echo Step 9: Updating Backend CORS configuration...
az containerapp update --name internhub-backend --resource-group %RESOURCE_GROUP% --set-env-vars "APP_FRONTEND_URL=https://!FRONTEND_URL!"

echo [OK] CORS configuration updated

REM Summary
echo.
echo ========================================
echo   Deployment Complete! 🎉
echo ========================================
echo.
echo Deployment Summary:
echo.
echo Frontend URL:  https://!FRONTEND_URL!
echo Backend URL:   https://!BACKEND_URL!
echo Database Host: !DB_HOST!
echo Redis Host:    !REDIS_HOST!
echo.
echo Saved Credentials:
echo Database Admin: !DB_ADMIN_USER!
echo JWT Secret: !JWT_SECRET!
echo.
echo Monitor your deployment:
echo Azure Portal: https://portal.azure.com
echo.
echo Next Steps:
echo 1. Test the application at: https://!FRONTEND_URL!
echo 2. Update mobile app API URL to: https://!BACKEND_URL!/api
echo 3. Build mobile APK (see MOBILE_APK_BUILD_GUIDE.md)
echo 4. Set up budget alerts in Azure Portal
echo.
echo To delete all resources:
echo az group delete --name %RESOURCE_GROUP% --yes
echo.

pause
