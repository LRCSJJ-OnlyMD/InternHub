# InternHub - Internship Management Platform

A comprehensive full-stack internship management system with web and mobile applications.

## 🚀 Quick Start for Azure Deployment

**Ready to deploy?** See [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) for detailed Azure deployment instructions.

**Key Points:**
- ✅ Keep monorepo structure - no need to split repositories
- ✅ Backend: Deploy to Azure Web App (Java 17)
- ✅ Frontend: Deploy to Azure Static Web App
- ✅ Database: Azure PostgreSQL Flexible Server (auto-created)
- ✅ Estimated cost: ~$25-30/month

## 🏗️ Architecture

### Technology Stack

**Backend:**
- Java 17 + Spring Boot 3.2
- Spring Security with JWT authentication
- Azure PostgreSQL (production) / PostgreSQL 16 (local)
- Azure Cache for Redis (optional) / Redis 7 (local)
- Email notifications (SMTP)
- REST API with comprehensive endpoints

**Frontend:**
- Angular 19 (latest)
- Angular Material UI
- Responsive design
- Internationalization (i18n)
- Real-time updates

**Mobile:**
- Flutter/Dart
- Cross-platform (Android/iOS)
- Native performance
- Offline support

**Infrastructure:**
- Azure Web Apps
- Azure Static Web Apps
- Azure PostgreSQL Flexible Server
- Docker & Docker Compose
- Multi-stage builds
- Health checks
- Volume persistence
- Azure-ready deployment

---

## 🚀 Quick Start (Local Development)

### Prerequisites
- Docker Desktop
- Git
- (Optional) Node.js, Java 17, Flutter SDK for local development

### Start All Services

```bash
# 1. Clone the repository
git clone https://github.com/LRCSJJ-OnlyMD/InternHub.git
cd InternHub

# 2. Configure environment (optional for local testing)
# cp .env.azure.template .env.local
# Edit .env.local with your settings

# 3. Start all services
docker-compose up -d --build

# 4. Access the application
# Frontend: http://localhost
# Backend API: http://localhost:8080/api
# Health Check: http://localhost:8080/actuator/health
```

### Default Ports
- **Frontend**: 80
- **Backend**: 8080
- **PostgreSQL**: 5432
- **Redis**: 6379

---

## ☁️ Azure Deployment

### Quick Deploy (Automated)

**Windows:**
```bash
deploy-azure.bat
```

**Linux/Mac:**
```bash
chmod +x deploy-azure.sh
./deploy-azure.sh
```

### Manual Deployment

See **[AZURE_DEPLOYMENT_GUIDE.md](./AZURE_DEPLOYMENT_GUIDE.md)** for comprehensive deployment instructions.

**Features:**
- Azure Container Apps deployment
- Managed PostgreSQL and Redis
- Automatic scaling
- HTTPS/SSL enabled
- Cost-optimized for $100 credit
- Estimated cost: $50-60/month

---

## 📱 Mobile App (APK Build)

The mobile app is not deployed to cloud services. Instead, build an APK and share via file hosting.

See **[MOBILE_APK_BUILD_GUIDE.md](./MOBILE_APK_BUILD_GUIDE.md)** for detailed instructions.

**Quick Build:**
```bash
cd mobile
flutter clean
flutter pub get
flutter build apk --release
# APK location: build/app/outputs/flutter-apk/app-release.apk
```

---

## 📊 Services Overview

### Backend (`/backend`)
- **Framework**: Spring Boot 3.2
- **Database**: PostgreSQL with JPA/Hibernate
- **Authentication**: JWT tokens, role-based access
- **Features**:
  - User management (Admin, Instructor, Student)
  - Internship CRUD operations
  - Document uploads
  - Email notifications
  - Comments and feedback system
  - Export to Excel/CSV

### Frontend (`/frontend`)
- **Framework**: Angular 19
- **UI Library**: Angular Material
- **Features**:
  - Responsive dashboard
  - Multi-language support (EN/FR)
  - Real-time data updates
  - File upload/download
  - Chart visualizations
  - Form validations

### Mobile (`/mobile`)
- **Framework**: Flutter
- **Features**:
  - Cross-platform support
  - Native performance
  - Push notifications ready
  - Offline data caching
  - Secure storage
  - Image picker integration

---

## 🔐 Key Features

### Authentication & Authorization
- ✅ JWT-based authentication
- ✅ Email verification
- ✅ Password reset
- ✅ Role-based access control (Admin, Instructor, Student)
- ✅ Secure password hashing

### Internship Management
- ✅ Create, update, delete internships
- ✅ Status tracking (Pending, Approved, In Progress, Completed)
- ✅ Document attachments
- ✅ Comment system
- ✅ Instructor assignment
- ✅ Approval workflow

### Reporting & Analytics
- ✅ Dashboard with statistics
- ✅ Export to Excel/CSV
- ✅ Search and filtering
- ✅ Data visualization

### Notifications
- ✅ Email notifications
- ✅ Status updates
- ✅ Assignment alerts

---

## 🛠️ Development

### Backend Development
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend Development
```bash
cd frontend
npm install
npm start
# Access at http://localhost:4200
```

### Mobile Development
```bash
cd mobile
flutter pub get
flutter run
```

---

## 🔍 API Documentation

### Authentication Endpoints
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `POST /api/auth/forgot-password` - Request password reset
- `POST /api/auth/reset-password` - Reset password

### Internship Endpoints
- `GET /api/internships` - List all internships
- `POST /api/internships` - Create internship
- `GET /api/internships/{id}` - Get internship details
- `PUT /api/internships/{id}` - Update internship
- `DELETE /api/internships/{id}` - Delete internship

### Admin Endpoints
- `GET /api/admin/users` - List all users
- `PUT /api/admin/users/{id}/role` - Update user role
- `GET /api/admin/statistics` - Get system statistics

See backend source code for complete API documentation.

---

## 📂 Project Structure

```
InternHub/
├── backend/                    # Spring Boot backend
│   ├── src/main/java/         # Java source code
│   ├── src/main/resources/    # Configuration files
│   ├── Dockerfile             # Backend Docker image
│   └── pom.xml               # Maven dependencies
├── frontend/                  # Angular frontend
│   ├── src/app/              # Angular components
│   ├── src/assets/           # Static assets
│   ├── Dockerfile            # Frontend Docker image
│   └── package.json          # NPM dependencies
├── mobile/                    # Flutter mobile app
│   ├── lib/                  # Dart source code
│   ├── android/              # Android configuration
│   ├── ios/                  # iOS configuration (if needed)
│   └── pubspec.yaml          # Flutter dependencies
├── docker-compose.yml         # Local development
├── docker-compose.azure.yml   # Azure deployment
├── AZURE_DEPLOYMENT_GUIDE.md  # Azure deployment docs
├── MOBILE_APK_BUILD_GUIDE.md  # APK build instructions
└── .env.azure.template        # Environment template
```

---

## 💰 Cost Estimation (Azure)

For $100 Azure credit deployment:

| Service | Tier | Monthly Cost |
|---------|------|--------------|
| Container Apps | Consumption | ~$15-20 |
| PostgreSQL Flexible | B1ms | ~$12 |
| Redis Cache | Basic C0 | ~$16 |
| Container Registry | Basic | ~$5 |
| Storage | Standard | ~$2 |
| **Total** | | **~$50-55** |

**Tips for staying within budget:**
- Use Container Apps (pay-per-use)
- Stop services when not presenting
- Set up budget alerts
- Use B-series burstable VMs

---

## 🔒 Security Best Practices

1. **Environment Variables**: Never commit `.env` files
2. **Secrets Management**: Use Azure Key Vault in production
3. **HTTPS**: Enforced automatically on Azure
4. **JWT Tokens**: Short expiration times
5. **Password Policy**: Strong password requirements
6. **SQL Injection**: Parameterized queries with JPA
7. **CORS**: Configured for frontend domain only
8. **File Uploads**: Size limits and type validation

---

## 🧪 Testing

### Backend Tests
```bash
cd backend
./mvnw test
```

### Frontend Tests
```bash
cd frontend
npm test
```

### Mobile Tests
```bash
cd mobile
flutter test
```

---

## 📞 Support & Documentation

- **Azure Deployment**: [AZURE_DEPLOYMENT_GUIDE.md](./AZURE_DEPLOYMENT_GUIDE.md)
- **Mobile APK Build**: [MOBILE_APK_BUILD_GUIDE.md](./MOBILE_APK_BUILD_GUIDE.md)
- **Login Credentials**: [LOGIN_CREDENTIALS.md](./LOGIN_CREDENTIALS.md)
- **Test Guide**: [test-guide.html](./test-guide.html)

---

## 📝 Pre-Presentation Checklist

### Azure Deployment
- [ ] Services deployed and running
- [ ] Frontend URL accessible (HTTPS)
- [ ] Backend API responding
- [ ] Database connection working
- [ ] Test with sample data
- [ ] Budget alerts configured

### Mobile App
- [ ] APK built successfully
- [ ] APK uploaded to MediaFire
- [ ] Download link tested
- [ ] Installation instructions ready
- [ ] App tested on device

### Demo Preparation
- [ ] Create demo user accounts
- [ ] Populate with sample data
- [ ] Test all key features
- [ ] Prepare backup (screenshots/videos)
- [ ] Document URLs and credentials

---

## 🎯 Deployment Timeline

**Day 1-2**: Azure Infrastructure
- Create Azure resources
- Deploy PostgreSQL and Redis
- Set up Container Registry

**Day 3**: Backend Deployment
- Build and push Docker image
- Deploy to Container Apps
- Configure environment variables
- Test API endpoints

**Day 4**: Frontend Deployment
- Build and push Docker image
- Deploy to Container Apps
- Configure API connection
- Test web application

**Day 5**: Mobile APK
- Update API endpoint
- Build release APK
- Upload to MediaFire
- Create installation guide

**Day 6-7**: Testing & Polish
- End-to-end testing
- Performance optimization
- Documentation review
- Presentation preparation

---

## 🤝 Contributing

This project is for academic purposes. For issues or improvements:
1. Create an issue
2. Fork the repository
3. Create a feature branch
4. Submit a pull request

---

## 📄 License

Academic project - All rights reserved

---

## 🏆 Credits

Developed as part of an academic project at M1.

**Technologies Used:**
- Spring Boot
- Angular
- Flutter
- PostgreSQL
- Redis
- Docker
- Microsoft Azure

---

**Good luck with your presentation! 🎉**
