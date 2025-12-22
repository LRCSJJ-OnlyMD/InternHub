# InternHub - Azure Deployment Guide

This guide will help you deploy the InternHub application to Microsoft Azure using Azure Container Apps or Azure Web App for Containers.

## 📋 Prerequisites

- Azure account with $100 credit
- Azure CLI installed ([Download here](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli))
- Docker Desktop installed
- Git installed

## 🎯 Deployment Options

### Option 1: Azure Container Apps (Recommended - Cost Effective)
Best for: Microservices, automatic scaling, pay-per-use

### Option 2: Azure Web App for Containers
Best for: Traditional web applications, easier management

---

## 🚀 Quick Start Deployment

### Step 1: Login to Azure

```bash
# Login to Azure
az login

# Set your subscription (if you have multiple)
az account set --subscription "Your Subscription Name"

# Create a resource group
az group create --name internhub-rg --location eastus
```

### Step 2: Prepare Environment Variables

Copy the `.env.azure.template` file to `.env.azure` and fill in your values:

```bash
cp .env.azure.template .env.azure
```

Edit `.env.azure` with your actual values (see configuration section below).

### Step 3: Deploy Using Azure Container Apps (Recommended)

```bash
# Install Azure Container Apps extension
az extension add --name containerapp --upgrade

# Create a Container Apps environment
az containerapp env create \
  --name internhub-env \
  --resource-group internhub-rg \
  --location eastus

# Create PostgreSQL Database
az postgres flexible-server create \
  --name internhub-db-server \
  --resource-group internhub-rg \
  --location eastus \
  --admin-user internhubadmin \
  --admin-password "YourSecurePassword123!" \
  --sku-name Standard_B1ms \
  --tier Burstable \
  --storage-size 32 \
  --version 16

# Create database
az postgres flexible-server db create \
  --resource-group internhub-rg \
  --server-name internhub-db-server \
  --database-name internhub

# Configure firewall to allow Azure services
az postgres flexible-server firewall-rule create \
  --resource-group internhub-rg \
  --name internhub-db-server \
  --rule-name AllowAzureServices \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0

# Create Azure Cache for Redis
az redis create \
  --name internhub-cache \
  --resource-group internhub-rg \
  --location eastus \
  --sku Basic \
  --vm-size c0

# Get Redis connection info
az redis show --name internhub-cache --resource-group internhub-rg
az redis list-keys --name internhub-cache --resource-group internhub-rg
```

### Step 4: Build and Push Docker Images

```bash
# Create Azure Container Registry
az acr create \
  --resource-group internhub-rg \
  --name internhubacr \
  --sku Basic \
  --admin-enabled true

# Get ACR credentials
az acr credential show --name internhubacr --resource-group internhub-rg

# Login to ACR
az acr login --name internhubacr

# Build and push backend image
cd backend
docker build -t internhubacr.azurecr.io/internhub-backend:latest .
docker push internhubacr.azurecr.io/internhub-backend:latest

# Build and push frontend image
cd ../frontend
docker build --build-arg API_URL=https://internhub-backend.REPLACE_WITH_YOUR_REGION.azurecontainerapps.io/api -t internhubacr.azurecr.io/internhub-frontend:latest .
docker push internhubacr.azurecr.io/internhub-frontend:latest

cd ..
```

### Step 5: Deploy Backend Container App

```bash
# Get database and redis connection strings
DB_HOST="internhub-db-server.postgres.database.azure.com"
REDIS_HOST=$(az redis show --name internhub-cache --resource-group internhub-rg --query hostName -o tsv)
REDIS_KEY=$(az redis list-keys --name internhub-cache --resource-group internhub-rg --query primaryKey -o tsv)

# Deploy backend
az containerapp create \
  --name internhub-backend \
  --resource-group internhub-rg \
  --environment internhub-env \
  --image internhubacr.azurecr.io/internhub-backend:latest \
  --registry-server internhubacr.azurecr.io \
  --registry-username <ACR_USERNAME> \
  --registry-password <ACR_PASSWORD> \
  --target-port 8080 \
  --ingress external \
  --min-replicas 1 \
  --max-replicas 2 \
  --cpu 1.0 \
  --memory 2.0Gi \
  --env-vars \
    SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOST}:5432/internhub?sslmode=require" \
    SPRING_DATASOURCE_USERNAME="internhubadmin" \
    SPRING_DATASOURCE_PASSWORD="YourSecurePassword123!" \
    SPRING_DATA_REDIS_HOST="${REDIS_HOST}" \
    SPRING_DATA_REDIS_PASSWORD="${REDIS_KEY}" \
    SPRING_DATA_REDIS_PORT="6380" \
    SPRING_DATA_REDIS_SSL="true" \
    JWT_SECRET="your-very-secure-jwt-secret-minimum-256-bits-long" \
    SPRING_MAIL_USERNAME="your-email@gmail.com" \
    SPRING_MAIL_PASSWORD="your-app-password" \
    APP_FRONTEND_URL="https://internhub-frontend.REPLACE.azurecontainerapps.io"

# Get backend URL
az containerapp show \
  --name internhub-backend \
  --resource-group internhub-rg \
  --query properties.configuration.ingress.fqdn \
  -o tsv
```

### Step 6: Deploy Frontend Container App

```bash
# Get backend URL
BACKEND_URL=$(az containerapp show --name internhub-backend --resource-group internhub-rg --query properties.configuration.ingress.fqdn -o tsv)

# Deploy frontend
az containerapp create \
  --name internhub-frontend \
  --resource-group internhub-rg \
  --environment internhub-env \
  --image internhubacr.azurecr.io/internhub-frontend:latest \
  --registry-server internhubacr.azurecr.io \
  --registry-username <ACR_USERNAME> \
  --registry-password <ACR_PASSWORD> \
  --target-port 80 \
  --ingress external \
  --min-replicas 1 \
  --max-replicas 2 \
  --cpu 0.5 \
  --memory 1.0Gi \
  --env-vars \
    API_URL="https://${BACKEND_URL}/api"

# Get frontend URL
az containerapp show \
  --name internhub-frontend \
  --resource-group internhub-rg \
  --query properties.configuration.ingress.fqdn \
  -o tsv
```

---

## 🔧 Alternative: Deploy Using Azure Web App for Containers

### Using Docker Compose (Simpler but more expensive)

```bash
# Create App Service Plan (B1 tier - approximately $13/month)
az appservice plan create \
  --name internhub-plan \
  --resource-group internhub-rg \
  --location eastus \
  --is-linux \
  --sku B1

# Create a Web App with Docker Compose
az webapp create \
  --resource-group internhub-rg \
  --plan internhub-plan \
  --name internhub-app \
  --multicontainer-config-type compose \
  --multicontainer-config-file docker-compose.azure.yml

# Configure environment variables
az webapp config appsettings set \
  --resource-group internhub-rg \
  --name internhub-app \
  --settings @.env.azure
```

---

## ⚙️ Configuration

### Required Environment Variables

Create a `.env.azure` file with the following variables:

```env
# Database Configuration
POSTGRES_DB=internhub
POSTGRES_USER=internhub_user
POSTGRES_PASSWORD=<STRONG_PASSWORD>

# Redis Configuration
REDIS_PASSWORD=<STRONG_PASSWORD>

# JWT Configuration
JWT_SECRET=<GENERATE_SECURE_256_BIT_SECRET>
JWT_EXPIRATION=86400000

# Mail Configuration (Gmail example)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=<GMAIL_APP_PASSWORD>

# Application URLs
FRONTEND_URL=https://your-frontend-url.azurecontainerapps.io
BACKEND_URL=https://your-backend-url.azurecontainerapps.io
```

### Generating Secure Secrets

```bash
# Generate JWT Secret
openssl rand -base64 64

# Generate passwords
openssl rand -base64 32
```

---

## 📱 Mobile App (APK)

The mobile app will not be deployed to Azure. Instead, build an APK file and share it via MediaFire or similar service.

See [MOBILE_APK_BUILD_GUIDE.md](./MOBILE_APK_BUILD_GUIDE.md) for detailed instructions.

---

## 💰 Cost Optimization Tips

### For $100 Azure Credit:

1. **Use Container Apps** (Pay only when running)
   - Estimated cost: $20-30/month for this app
   
2. **Database: Azure PostgreSQL Flexible Server**
   - Burstable tier (B1ms): ~$12/month
   
3. **Cache: Azure Cache for Redis Basic**
   - Basic C0: ~$16/month
   
4. **Container Registry Basic**
   - Basic tier: ~$5/month

**Total estimated monthly cost: $50-60**

### To stay within budget:
- Use free tier services where possible
- Stop resources when not actively presenting
- Set up budget alerts in Azure Portal
- Use B-series VMs (burstable performance)

---

## 🔍 Monitoring and Troubleshooting

### View Logs

```bash
# Backend logs
az containerapp logs show \
  --name internhub-backend \
  --resource-group internhub-rg \
  --follow

# Frontend logs
az containerapp logs show \
  --name internhub-frontend \
  --resource-group internhub-rg \
  --follow
```

### Scale Applications

```bash
# Scale backend
az containerapp update \
  --name internhub-backend \
  --resource-group internhub-rg \
  --min-replicas 1 \
  --max-replicas 3

# Scale frontend
az containerapp update \
  --name internhub-frontend \
  --resource-group internhub-rg \
  --min-replicas 1 \
  --max-replicas 3
```

### Health Checks

```bash
# Check backend health
curl https://your-backend-url.azurecontainerapps.io/actuator/health

# Check frontend
curl https://your-frontend-url.azurecontainerapps.io
```

---

## 🛡️ Security Best Practices

1. **Never commit `.env.azure` to Git** (already in .gitignore)
2. Use Azure Key Vault for sensitive data in production
3. Enable HTTPS (automatic with Container Apps)
4. Configure CORS properly in backend
5. Use strong passwords and secrets
6. Enable Azure Defender for security monitoring

---

## 🔄 Update Deployment

```bash
# Rebuild and push new images
docker build -t internhubacr.azurecr.io/internhub-backend:latest ./backend
docker push internhubacr.azurecr.io/internhub-backend:latest

docker build -t internhubacr.azurecr.io/internhub-frontend:latest ./frontend
docker push internhubacr.azurecr.io/internhub-frontend:latest

# Update container apps
az containerapp update \
  --name internhub-backend \
  --resource-group internhub-rg \
  --image internhubacr.azurecr.io/internhub-backend:latest

az containerapp update \
  --name internhub-frontend \
  --resource-group internhub-rg \
  --image internhubacr.azurecr.io/internhub-frontend:latest
```

---

## 🗑️ Cleanup Resources

```bash
# Delete entire resource group (removes all resources)
az group delete --name internhub-rg --yes --no-wait
```

---

## 📞 Support

For issues or questions:
- Check Azure documentation: https://docs.microsoft.com/azure
- Review container logs for error messages
- Ensure all environment variables are correctly set
- Verify database and Redis connections

---

## 📝 Pre-Presentation Checklist

- [ ] Application deployed and accessible via HTTPS
- [ ] Database populated with demo data
- [ ] Mobile APK built and uploaded to MediaFire
- [ ] All credentials secured (not exposed)
- [ ] Budget alerts configured in Azure Portal
- [ ] Backup of database created
- [ ] Application tested end-to-end
- [ ] URLs documented for presentation

---

**Good luck with your presentation! 🎉**
