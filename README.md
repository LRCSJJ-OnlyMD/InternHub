# InternHub - Internship Management Platform

## 🏗️ Enterprise Architecture

### Technology Stack

**Backend:**
- Java 17 + Spring Boot 3.2
- Spring Security with JWT
- **Prisma Accelerate** (Managed PostgreSQL)
- **Redis 7** for caching & emergency backup
- Google Authenticator (TOTP 2FA)
- ZXing QR code generation

**Frontend:**
- Angular 17
- Modern CSS design system
- Responsive UI/UX

**Infrastructure:**
- Docker & Docker Compose
- Multi-stage optimized builds
- Health monitoring

---

## 💾 Data Resilience Architecture

### Primary: Prisma Accelerate PostgreSQL
- Global edge caching
- Automatic connection pooling
- Low-latency access worldwide
- Managed failover

### Backup: Redis Emergency System  
- **24-hour user data cache**
- Verification token storage
- Quick recovery if primary is down
- Auto-sync when restored

**Architecture Flow:**
```
User Request
    ↓
Spring Boot API
    ↓
Check Redis Cache (Fast Path) → Hit: Return cached data
    ↓ Miss
Query Prisma DB (Primary) → Cache to Redis → Return data
    ↓ DB Down
Serve from Redis Backup (Emergency Mode)
```

---

## 🚀 Quick Start

```bash
# 1. Set environment variables
echo "MAIL_USERNAME=your-email@gmail.com" > .env
echo "MAIL_PASSWORD=your-gmail-app-password" >> .env

# 2. Start all services
docker-compose up -d --build

# 3. Access application
# Frontend: http://localhost:4200
# Backend: http://localhost:8080
# Health: http://localhost:8080/api/health
```

---

## 📊 Services

- **auth-redis**: Redis 7 (Port 6379) - Caching & backup
- **auth-postgres**: PostgreSQL 16 (Port 5432) - Local fallback
- **auth-backend**: Spring Boot (Port 8080) - REST API
- **auth-frontend**: Angular + Nginx (Port 4200) - Web UI

---

## 🔐 Features

✅ JWT Authentication  
✅ Email Verification  
✅ Password Reset  
✅ Two-Factor Authentication (2FA)  
✅ Redis Backup System  
✅ Prisma Accelerate Integration  
✅ Health Monitoring  
✅ Modern UI Design

---

## 🛠️ Health Monitoring

```bash
# Check system health
curl http://localhost:8080/api/health

# Response:
{
  "status": "UP",
  "redis": "UP",
  "backup": "AVAILABLE"
}
```

---

## 📡 API Endpoints

**Auth:** `/api/auth/*`  
**2FA:** `/api/auth/2fa/*`  
**Health:** `/api/health`, `/api/health/redis`

---

For detailed documentation, troubleshooting, and deployment guide, see inline comments in source files.
