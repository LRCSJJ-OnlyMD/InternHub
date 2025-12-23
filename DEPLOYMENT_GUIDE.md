# InternHub Azure Deployment Guide

## 📋 Prerequisites

- Azure account with active subscription
- GitHub repository (LRCSJJ-OnlyMD/InternHub)
- Azure CLI installed (optional)

---

## 🚀 Deployment Steps

### 1. Create Azure PostgreSQL Database

1. Go to Azure Portal → Create a resource → Azure Database for PostgreSQL
2. Select **Flexible Server**
3. Configure:
   - **Server name**: `internhub-db` (or your choice)
   - **Region**: France Central
   - **Version**: PostgreSQL 16
   - **Compute + storage**: Burstable, B1ms (cheapest for testing)
   - **Authentication**: Use your admin username/password
4. **Networking**: 
   - Allow public access
   - Add your IP to firewall rules
   - **Enable "Allow Azure services"** ✅
5. Create the database

### 2. Deploy Backend (Spring Boot)

#### Option A: Azure Web App for Containers (Recommended)

1. Go to Azure Portal → Create **Web App**
2. Configure:
   - **Name**: `internhub-backend`
   - **Publish**: Docker Container
   - **Operating System**: Linux
   - **Region**: France Central
   - **Pricing plan**: Basic B1
3. **Docker** tab:
   - **Options**: Single Container
   - **Image Source**: GitHub Actions (we'll configure this)
4. Create the app

#### Configure GitHub Actions for Backend:

Create `.github/workflows/deploy-backend.yml`:

```yaml
name: Deploy Backend to Azure

on:
  push:
    branches: [master]
    paths:
      - 'backend/**'
  workflow_dispatch:

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build with Maven
      run: |
        cd backend
        mvn clean package -DskipTests
    
    - name: Deploy to Azure Web App
      uses: azure/webapps-deploy@v2
      with:
        app-name: 'internhub-backend'
        publish-profile: \${{ secrets.AZURE_BACKEND_PUBLISH_PROFILE }}
        package: 'backend/target/*.jar'
```

5. In Azure Portal, go to your backend Web App → **Deployment Center**
6. Download the **Publish Profile**
7. In GitHub → Settings → Secrets → Add `AZURE_BACKEND_PUBLISH_PROFILE`

#### Set Environment Variables in Azure:

Go to Web App → Configuration → Application settings:

```
DB_URL=jdbc:postgresql://internhub-db.postgres.database.azure.com:5432/internhub?sslmode=require
DB_USERNAME=your_admin@internhub-db
DB_PASSWORD=your_password
JWT_SECRET=your-secure-jwt-secret-key
FRONTEND_URL=https://internhub.azurewebsites.net
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### 3. Deploy Frontend (Angular)

#### Option A: Azure Static Web Apps (Cheapest)

1. Go to Azure Portal → Create **Static Web App**
2. Configure:
   - **Name**: `internhub`
   - **Region**: West Europe (closest to France)
   - **Deployment**: GitHub
   - **Repository**: LRCSJJ-OnlyMD/InternHub
   - **Branch**: master
   - **Build Presets**: Angular
   - **App location**: `/frontend`
   - **Output location**: `dist/internhub/browser`
3. Create

Azure will automatically create a GitHub Actions workflow file.

#### Update API URL:

Edit `frontend/src/environments/environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://internhub-backend.azurewebsites.net/api'
};
```

### 4. Configure CORS in Backend

Ensure your backend allows the frontend domain. In your Spring Boot CORS config:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("https://internhub.azurewebsites.net")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowCredentials(true);
    }
}
```

---

## 🐳 Alternative: Docker Compose Deployment

If you want to deploy both frontend and backend together using Docker:

### Prerequisites:
- Azure Container Registry (ACR) or Docker Hub
- Azure Container Instances or Azure App Service with Container support

### Steps:

1. **Build and push images**:
```bash
# Login to Azure Container Registry
az acr login --name internhubregistry

# Build and push backend
cd backend
docker build -t internhubregistry.azurecr.io/internhub-backend:latest .
docker push internhubregistry.azurecr.io/internhub-backend:latest

# Build and push frontend
cd ../frontend
docker build -t internhubregistry.azurecr.io/internhub-frontend:latest .
docker push internhubregistry.azurecr.io/internhub-frontend:latest
```

2. **Deploy using Azure Container Instances**:
```bash
az container create --resource-group internhub-rg \
  --file docker-compose.yml
```

---

## ✅ Post-Deployment Checklist

- [ ] Database connection works
- [ ] Backend health endpoint accessible: `https://internhub-backend.azurewebsites.net/actuator/health`
- [ ] Frontend loads correctly
- [ ] Login functionality works
- [ ] API calls from frontend to backend succeed
- [ ] CORS configured properly
- [ ] SSL/HTTPS enabled

---

## 🔧 Environment Variables Summary

### Backend (.env):
```
DB_URL=jdbc:postgresql://<server>.postgres.database.azure.com:5432/internhub?sslmode=require
DB_USERNAME=<admin>@<server>
DB_PASSWORD=<password>
JWT_SECRET=<long-secure-key>
FRONTEND_URL=https://internhub.azurewebsites.net
MAIL_USERNAME=<email>
MAIL_PASSWORD=<app-password>
```

### Frontend:
Update `environment.prod.ts`:
```typescript
apiUrl: 'https://internhub-backend.azurewebsites.net/api'
```

---

## 💰 Estimated Monthly Costs

- **Azure PostgreSQL Flexible (B1ms)**: ~$10-15/month
- **Backend Web App (B1)**: ~$13/month
- **Frontend Static Web App**: FREE (Free tier)

**Total**: ~$25-30/month

---

## 🆘 Troubleshooting

### Backend not connecting to database:
- Check firewall rules in PostgreSQL
- Ensure "Allow Azure services" is enabled
- Verify connection string format

### Frontend can't reach backend:
- Check CORS configuration
- Verify API_URL in environment.prod.ts
- Check network tab in browser dev tools

### GitHub Actions failing:
- Check secrets are properly set
- Verify publish profile is correct
- Check build logs for errors

---

## 📚 Useful Commands

```bash
# Check backend logs
az webapp log tail --name internhub-backend --resource-group <your-rg>

# Restart backend
az webapp restart --name internhub-backend --resource-group <your-rg>

# Test database connection
psql -h <server>.postgres.database.azure.com -U <admin> -d internhub
```
