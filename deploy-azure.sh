#!/bin/bash

# InternHub - Azure Quick Deployment Script
# This script helps deploy InternHub to Azure Container Apps

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
RESOURCE_GROUP="internhub-rg"
LOCATION="eastus"
ACR_NAME="internhubacr"
DB_SERVER_NAME="internhub-db-server"
REDIS_NAME="internhub-cache"
ENV_NAME="internhub-env"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  InternHub Azure Deployment Script${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if Azure CLI is installed
if ! command -v az &> /dev/null; then
    echo -e "${RED}Error: Azure CLI is not installed${NC}"
    echo "Please install it from: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli"
    exit 1
fi

# Check if logged in
echo -e "${YELLOW}Checking Azure login status...${NC}"
if ! az account show &> /dev/null; then
    echo -e "${YELLOW}Not logged in. Opening Azure login...${NC}"
    az login
fi

echo -e "${GREEN}✓ Azure CLI logged in${NC}"
echo ""

# List subscriptions
echo -e "${YELLOW}Available subscriptions:${NC}"
az account list --output table
echo ""

read -p "Enter subscription ID (or press Enter to use default): " SUBSCRIPTION_ID
if [ ! -z "$SUBSCRIPTION_ID" ]; then
    az account set --subscription "$SUBSCRIPTION_ID"
    echo -e "${GREEN}✓ Subscription set${NC}"
fi

echo ""
echo -e "${YELLOW}Using Resource Group: ${RESOURCE_GROUP}${NC}"
echo -e "${YELLOW}Using Location: ${LOCATION}${NC}"
echo ""

read -p "Do you want to proceed? (y/n): " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Deployment cancelled."
    exit 0
fi

# Create Resource Group
echo ""
echo -e "${BLUE}Step 1: Creating Resource Group...${NC}"
az group create --name $RESOURCE_GROUP --location $LOCATION
echo -e "${GREEN}✓ Resource Group created${NC}"

# Create Container Apps Environment
echo ""
echo -e "${BLUE}Step 2: Creating Container Apps Environment...${NC}"
az extension add --name containerapp --upgrade
az containerapp env create \
    --name $ENV_NAME \
    --resource-group $RESOURCE_GROUP \
    --location $LOCATION
echo -e "${GREEN}✓ Container Apps Environment created${NC}"

# Create PostgreSQL Database
echo ""
echo -e "${BLUE}Step 3: Creating PostgreSQL Database...${NC}"
read -p "Enter PostgreSQL admin username: " DB_ADMIN_USER
read -sp "Enter PostgreSQL admin password: " DB_ADMIN_PASSWORD
echo ""

az postgres flexible-server create \
    --name $DB_SERVER_NAME \
    --resource-group $RESOURCE_GROUP \
    --location $LOCATION \
    --admin-user $DB_ADMIN_USER \
    --admin-password "$DB_ADMIN_PASSWORD" \
    --sku-name Standard_B1ms \
    --tier Burstable \
    --storage-size 32 \
    --version 16 \
    --public-access 0.0.0.0-0.0.0.0

az postgres flexible-server db create \
    --resource-group $RESOURCE_GROUP \
    --server-name $DB_SERVER_NAME \
    --database-name internhub

echo -e "${GREEN}✓ PostgreSQL Database created${NC}"

# Create Redis Cache
echo ""
echo -e "${BLUE}Step 4: Creating Redis Cache...${NC}"
az redis create \
    --name $REDIS_NAME \
    --resource-group $RESOURCE_GROUP \
    --location $LOCATION \
    --sku Basic \
    --vm-size c0

REDIS_HOST=$(az redis show --name $REDIS_NAME --resource-group $RESOURCE_GROUP --query hostName -o tsv)
REDIS_KEY=$(az redis list-keys --name $REDIS_NAME --resource-group $RESOURCE_GROUP --query primaryKey -o tsv)

echo -e "${GREEN}✓ Redis Cache created${NC}"

# Create Container Registry
echo ""
echo -e "${BLUE}Step 5: Creating Container Registry...${NC}"
az acr create \
    --resource-group $RESOURCE_GROUP \
    --name $ACR_NAME \
    --sku Basic \
    --admin-enabled true

ACR_USERNAME=$(az acr credential show --name $ACR_NAME --resource-group $RESOURCE_GROUP --query username -o tsv)
ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --resource-group $RESOURCE_GROUP --query passwords[0].value -o tsv)

echo -e "${GREEN}✓ Container Registry created${NC}"

# Build and Push Docker Images
echo ""
echo -e "${BLUE}Step 6: Building and Pushing Docker Images...${NC}"
az acr login --name $ACR_NAME

echo "Building backend..."
docker build -t ${ACR_NAME}.azurecr.io/internhub-backend:latest ./backend
docker push ${ACR_NAME}.azurecr.io/internhub-backend:latest

echo "Building frontend..."
docker build -t ${ACR_NAME}.azurecr.io/internhub-frontend:latest ./frontend
docker push ${ACR_NAME}.azurecr.io/internhub-frontend:latest

echo -e "${GREEN}✓ Docker images pushed${NC}"

# Get JWT Secret
echo ""
read -sp "Enter JWT Secret (or press Enter to generate): " JWT_SECRET
echo ""
if [ -z "$JWT_SECRET" ]; then
    JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
    echo -e "${YELLOW}Generated JWT Secret: ${JWT_SECRET}${NC}"
fi

# Get Email Configuration
echo ""
echo -e "${YELLOW}Email Configuration (for password reset):${NC}"
read -p "Enter SMTP host (default: smtp.gmail.com): " MAIL_HOST
MAIL_HOST=${MAIL_HOST:-smtp.gmail.com}
read -p "Enter SMTP port (default: 587): " MAIL_PORT
MAIL_PORT=${MAIL_PORT:-587}
read -p "Enter email username: " MAIL_USERNAME
read -sp "Enter email password: " MAIL_PASSWORD
echo ""

# Deploy Backend
echo ""
echo -e "${BLUE}Step 7: Deploying Backend Container App...${NC}"
DB_HOST="${DB_SERVER_NAME}.postgres.database.azure.com"

az containerapp create \
    --name internhub-backend \
    --resource-group $RESOURCE_GROUP \
    --environment $ENV_NAME \
    --image ${ACR_NAME}.azurecr.io/internhub-backend:latest \
    --registry-server ${ACR_NAME}.azurecr.io \
    --registry-username $ACR_USERNAME \
    --registry-password $ACR_PASSWORD \
    --target-port 8080 \
    --ingress external \
    --min-replicas 1 \
    --max-replicas 2 \
    --cpu 1.0 \
    --memory 2.0Gi \
    --env-vars \
        "SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_HOST}:5432/internhub?sslmode=require" \
        "SPRING_DATASOURCE_USERNAME=${DB_ADMIN_USER}" \
        "SPRING_DATASOURCE_PASSWORD=${DB_ADMIN_PASSWORD}" \
        "SPRING_DATA_REDIS_HOST=${REDIS_HOST}" \
        "SPRING_DATA_REDIS_PASSWORD=${REDIS_KEY}" \
        "SPRING_DATA_REDIS_PORT=6380" \
        "SPRING_DATA_REDIS_SSL=true" \
        "JWT_SECRET=${JWT_SECRET}" \
        "SPRING_MAIL_HOST=${MAIL_HOST}" \
        "SPRING_MAIL_PORT=${MAIL_PORT}" \
        "SPRING_MAIL_USERNAME=${MAIL_USERNAME}" \
        "SPRING_MAIL_PASSWORD=${MAIL_PASSWORD}"

BACKEND_URL=$(az containerapp show --name internhub-backend --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv)
echo -e "${GREEN}✓ Backend deployed at: https://${BACKEND_URL}${NC}"

# Deploy Frontend
echo ""
echo -e "${BLUE}Step 8: Deploying Frontend Container App...${NC}"

az containerapp create \
    --name internhub-frontend \
    --resource-group $RESOURCE_GROUP \
    --environment $ENV_NAME \
    --image ${ACR_NAME}.azurecr.io/internhub-frontend:latest \
    --registry-server ${ACR_NAME}.azurecr.io \
    --registry-username $ACR_USERNAME \
    --registry-password $ACR_PASSWORD \
    --target-port 80 \
    --ingress external \
    --min-replicas 1 \
    --max-replicas 2 \
    --cpu 0.5 \
    --memory 1.0Gi \
    --env-vars \
        "API_URL=https://${BACKEND_URL}/api"

FRONTEND_URL=$(az containerapp show --name internhub-frontend --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv)
echo -e "${GREEN}✓ Frontend deployed at: https://${FRONTEND_URL}${NC}"

# Update Backend with Frontend URL
echo ""
echo -e "${BLUE}Step 9: Updating Backend CORS configuration...${NC}"
az containerapp update \
    --name internhub-backend \
    --resource-group $RESOURCE_GROUP \
    --set-env-vars "APP_FRONTEND_URL=https://${FRONTEND_URL}"

echo -e "${GREEN}✓ CORS configuration updated${NC}"

# Summary
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Deployment Complete! 🎉${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${YELLOW}📝 Deployment Summary:${NC}"
echo ""
echo -e "Frontend URL:  ${BLUE}https://${FRONTEND_URL}${NC}"
echo -e "Backend URL:   ${BLUE}https://${BACKEND_URL}${NC}"
echo -e "Database Host: ${DB_HOST}"
echo -e "Redis Host:    ${REDIS_HOST}"
echo ""
echo -e "${YELLOW}🔐 Saved Credentials:${NC}"
echo "Database Admin: ${DB_ADMIN_USER}"
echo "JWT Secret: ${JWT_SECRET}"
echo ""
echo -e "${YELLOW}📊 Monitor your deployment:${NC}"
echo "Azure Portal: https://portal.azure.com"
echo ""
echo -e "${YELLOW}💡 Next Steps:${NC}"
echo "1. Test the application at: https://${FRONTEND_URL}"
echo "2. Update mobile app API URL to: https://${BACKEND_URL}/api"
echo "3. Build mobile APK (see MOBILE_APK_BUILD_GUIDE.md)"
echo "4. Set up budget alerts in Azure Portal"
echo ""
echo -e "${YELLOW}🗑️  To delete all resources:${NC}"
echo "az group delete --name ${RESOURCE_GROUP} --yes"
echo ""
