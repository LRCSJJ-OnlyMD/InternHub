# InternHub - Login Credentials

## 🔐 Default Test Accounts

Use these credentials to test the application. All accounts are pre-seeded on first run.

---

### 👨‍💼 Admin Account

| Field | Value |
|-------|-------|
| **Email** | `admin@internship.com` |
| **Password** | `Admin123!` |
| **Role** | Administrator |

**Admin Capabilities:**
- Manage all users (create, edit, delete)
- View all internships
- Assign instructors to sectors
- View system statistics
- Export data to Excel/CSV

---

### 👨‍🏫 Instructor Accounts

All instructors use the password: **`Password123!`**

| Email | Name | Assigned Sectors |
|-------|------|------------------|
| `prof.hassan@instructor.ma` | Hassan Alami | IT, Engineering |
| `prof.sarah@instructor.ma` | Sarah Bennani | Finance, Marketing |
| `prof.omar@instructor.ma` | Omar Idrissi | IT, Healthcare |
| `prof.latifa@instructor.ma` | Latifa Bouazza | HR, Legal |
| `prof.karim@instructor.ma` | Karim Tazi | Engineering, Healthcare |
| `prof.nadia@instructor.ma` | Nadia El Fassi | Marketing, HR, Legal |

**Instructor Capabilities:**
- View assigned student internships
- Approve or reject internship requests
- Add comments and feedback
- Upload documents
- Track student progress

---

### 👨‍🎓 Student Accounts

All students use the password: **`Password123!`**

| Email | Name |
|-------|------|
| `alice.martin@student.ma` | Alice Martin |
| `bob.dupont@student.ma` | Bob Dupont |
| `claire.bernard@student.ma` | Claire Bernard |
| `david.rousseau@student.ma` | David Rousseau |
| `emma.petit@student.ma` | Emma Petit |
| `fatima.zahir@student.ma` | Fatima Zahir |

**Student Capabilities:**
- Create new internship requests
- Upload required documents
- Track internship status
- View comments from instructors
- Update profile information

---

## 🏢 Available Sectors

| Code | Sector Name |
|------|-------------|
| IT | Information Technology |
| FIN | Finance & Banking |
| MKT | Marketing & Communication |
| HR | Human Resources |
| ENG | Engineering & Manufacturing |
| HEALTH | Healthcare & Pharmaceuticals |
| LAW | Legal & Compliance |

---

## 📊 Pre-Loaded Test Data

The application is seeded with:
- **16 Internships** with various statuses:
  - 5 Validated ✅
  - 5 Pending ⏳
  - 3 Refused ❌
  - 3 Drafts 📝

---

## 🌐 Access URLs

### Local Development
- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8080/api
- **Health Check**: http://localhost:8080/actuator/health

### Production (Cloud Deployment)
- Configure your own URLs after deployment

---

## ⚠️ Security Notice

**These are test credentials only!**

For production deployment:
1. Change the admin password immediately
2. Use strong, unique passwords
3. Enable two-factor authentication
4. Use environment variables for sensitive data
5. Never commit real credentials to version control

---

## 🔧 Password Requirements

When creating new accounts, passwords must:
- Be at least 8 characters long
- Contain at least one uppercase letter
- Contain at least one lowercase letter
- Contain at least one number
- Contain at least one special character (!@#$%^&*)

---

*Last updated: January 2026*
