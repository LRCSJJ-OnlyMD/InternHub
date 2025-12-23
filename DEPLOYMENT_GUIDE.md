# InternHub - Deployment Guide (Neon + Koyeb + Vercel)

## 🎯 **Your Deployment Stack**

- **Database**: Neon PostgreSQL (Serverless, Free tier available)
- **Backend**: Koyeb (Supports Docker & monorepos)
- **Frontend**: Vercel (Perfect for Angular)
- **Repository**: Keep monorepo structure ✅

---

## 📋 **Prerequisites**

- GitHub account with InternHub repository
- Neon account (https://console.neon.tech)
- Koyeb account (https://app.koyeb.com)
- Vercel account (https://vercel.com)

---

## 🚀 **STEP 1: Create Neon PostgreSQL Database**

### 1. Sign up at Neon
Go to https://console.neon.tech and create account (free tier available)

### 2. Create a New Project
- Click **"New Project"**
- **Name**: `internhub`
- **Region**: Choose closest to your users
- **PostgreSQL version**: 16 (latest)
- Click **"Create Project"**

### 3. Get Connection Details
After creation, you'll see a connection string like:
```
postgresql://username:password@ep-example-123.neon.tech:5432/neondb?sslmode=require
```

### 4. Convert to JDBC Format
Change to JDBC format for Spring Boot:
```
jdbc:postgresql://ep-example-123.neon.tech:5432/neondb?sslmode=require
```

**Save these credentials:**
- DB_URL: `jdbc:postgresql://ep-example-123.neon.tech:5432/neondb?sslmode=require`
- DB_USERNAME: (shown in Neon dashboard)
- DB_PASSWORD: (shown in Neon dashboard)

---

## 🔧 **STEP 2: Deploy Backend to Koyeb**

### 1. Sign up at Koyeb
Go to https://app.koyeb.com and create account

### 2. Create New App
- Click **"Create App"**
- Select **"GitHub"** as source

### 3. Connect GitHub Repository
- Authorize Koyeb to access your GitHub
- Select repository: `LRCSJJ-OnlyMD/InternHub`
- **Branch**: `master`

### 4. Configure Build Settings
- **Builder**: Docker
- **Dockerfile path**: `backend/Dockerfile`
- **Docker build context**: `backend`

### 5. Configure Environment Variables
Click **"Environment variables"** and add:

```
DB_URL=jdbc:postgresql://ep-YOUR-ENDPOINT.neon.tech:5432/neondb?sslmode=require
DB_USERNAME=your-neon-username
DB_PASSWORD=your-neon-password
JWT_SECRET=mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForHS512Algorithm
MAIL_USERNAME=mouadom2003@gmail.com
MAIL_PASSWORD=gxehbdwxkvvelkmz
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
FRONTEND_URL=https://internhub.vercel.app
```

### 6. Configure Service Settings
- **Service name**: `internhub-backend`
- **Instance type**: Free (or Nano $5/month)
- **Region**: Choose same as Neon database
- **Port**: `8080`
- **Health check path**: `/actuator/health`

### 7. Deploy
- Click **"Deploy"**
- Wait 3-5 minutes for build to complete

### 8. Get Your Backend URL
After deployment, your URL will be:
```
https://internhub-backend-<your-org>.koyeb.app
```

**Copy this URL!** You'll need it for frontend.

---

## 🎨 **STEP 3: Deploy Frontend to Vercel**

### 1. Sign up at Vercel
Go to https://vercel.com and sign up with GitHub

### 2. Import Project
- Click **"Add New"** → **"Project"**
- Select `LRCSJJ-OnlyMD/InternHub` repository
- Click **"Import"**

### 3. Configure Project Settings
- **Framework Preset**: Angular
- **Root Directory**: `frontend`
- **Build Command**: `npm run build`
- **Output Directory**: `dist/internhub/browser`

### 4. Add Environment Variable
Click **"Environment Variables"** and add:
```
API_URL=https://internhub-backend-<your-org>.koyeb.app/api
```

### 5. Deploy
- Click **"Deploy"**
- Wait 2-3 minutes

### 6. Get Your Frontend URL
After deployment:
```
https://internhub.vercel.app
```
(or your custom domain)

---

## 🔄 **STEP 4: Update CORS Configuration**

### Update Backend Environment Variable in Koyeb:
Go back to Koyeb → Your App → Settings → Environment Variables

Update `FRONTEND_URL`:
```
FRONTEND_URL=https://internhub.vercel.app
```

Click **"Redeploy"** to apply changes.

---

## ✅ **STEP 5: Test Your Deployment**

### 1. Test Backend Health
Open: `https://internhub-backend-<your-org>.koyeb.app/actuator/health`

Should show:
```json
{"status":"UP"}
```

### 2. Test Frontend
Open: `https://internhub.vercel.app`

You should see the login page.

### 3. Test Full Flow
1. Register a new user
2. Login
3. Check if API calls work (open browser DevTools → Network tab)

---

## 💰 **Monthly Costs**

| Service | Plan | Monthly Cost |
|---------|------|--------------|
| Neon PostgreSQL | Free tier | **$0** |
| Koyeb Backend | Free tier | **$0** |
| Vercel Frontend | Hobby | **$0** |
| **Total** | | **FREE!** ✅ |

**Paid tiers if needed:**
- Neon: From $20/month
- Koyeb: From $5/month
- Vercel: From $20/month

---

## 🔧 **Updating After Changes**

### Backend Changes:
```bash
git add backend/
git commit -m "Update backend"
git push origin master
```
Koyeb will auto-deploy (takes 3-5 min)

### Frontend Changes:
```bash
git add frontend/
git commit -m "Update frontend"
git push origin master
```
Vercel will auto-deploy (takes 2-3 min)

---

## 🆘 **Troubleshooting**

### Backend build fails on Koyeb:
- Check Dockerfile path is correct: `backend/Dockerfile`
- Check build context is set to `backend`
- View build logs in Koyeb dashboard

### Frontend can't reach backend:
- Check API_URL in Vercel environment variables
- Check FRONTEND_URL in Koyeb environment variables
- Open browser DevTools → Console for CORS errors

### Database connection fails:
- Verify connection string format (must start with `jdbc:postgresql://`)
- Check Neon database is active
- Ensure `?sslmode=require` is in connection string

### Application errors:
- Check logs in Koyeb dashboard
- Check browser console for frontend errors
- Verify all environment variables are set correctly

---

## 📚 **Quick Reference**

### Neon Dashboard:
https://console.neon.tech

### Koyeb Dashboard:
https://app.koyeb.com

### Vercel Dashboard:
https://vercel.com/dashboard

### Your URLs:
- **Backend**: `https://internhub-backend-<org>.koyeb.app`
- **Frontend**: `https://internhub.vercel.app`
- **Health Check**: `https://internhub-backend-<org>.koyeb.app/actuator/health`

---

## 🎉 **You're Done!**

Your InternHub application is now deployed and running on:
- ✅ Neon PostgreSQL (serverless database)
- ✅ Koyeb (backend)
- ✅ Vercel (frontend)

All with FREE tiers! 🎊
