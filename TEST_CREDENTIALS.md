# InternHub - Test Credentials

## 🔐 Login Information

**Default password for all accounts:** `Password123!`
**Admin account password:** `Admin123!`

---

## 👤 Admin Account

| Field | Value |
|-------|-------|
| Email | `mouadom2003@gmail.com` |
| Password | `Admin123!` |
| Role | ADMIN |

---

## 👨‍🏫 Instructor Accounts

| Name | Email | Password |
|------|-------|----------|
| Hassan Alami | `prof.hassan@instructor.ma` | `Password123!` |
| Sarah Bennani | `prof.sarah@instructor.ma` | `Password123!` |
| Omar Idrissi | `prof.omar@instructor.ma` | `Password123!` |
| Latifa Bouazza | `prof.latifa@instructor.ma` | `Password123!` |
| Karim Tazi | `prof.karim@instructor.ma` | `Password123!` |
| Nadia El Fassi | `prof.nadia@instructor.ma` | `Password123!` |

---

## 👨‍🎓 Student Accounts

| Name | Email | Password |
|------|-------|----------|
| Alice Martin | `alice.martin@student.ma` | `Password123!` |
| Bob Dupont | `bob.dupont@student.ma` | `Password123!` |
| Claire Bernard | `claire.bernard@student.ma` | `Password123!` |
| David Rousseau | `david.rousseau@student.ma` | `Password123!` |
| Emma Petit | `emma.petit@student.ma` | `Password123!` |
| Fatima Zahir | `fatima.zahir@student.ma` | `Password123!` |

---

## 🌐 Application URLs

| Service | URL |
|---------|-----|
| Frontend | http://localhost |
| Backend API | http://localhost:8080/api |
| Health Check | http://localhost:8080/actuator/health |
| Database | localhost:5432 (database: `internhub`) |

---

## 📊 Test Data Summary

### Sectors Available

- Information Technology (IT)
- Finance & Banking (FIN)
- Marketing & Communication (MKT)
- Human Resources (HR)
- Engineering & Manufacturing (ENG)
- Healthcare & Pharmaceuticals (HEALTH)
- Legal & Compliance (LAW)

### Internships

The DataSeeder creates sample internships with various statuses:
- Validated internships
- Pending validation
- Refused internships
- Draft internships

---

## 🧪 Testing Scenarios

### As Admin (`mouadom2003@gmail.com` / `Admin123!`)
- View all users and internships
- Manage instructors and students
- Access system-wide reports
- Configure sectors

### As Instructor (e.g., `prof.hassan@instructor.ma` / `Password123!`)
- Review pending internships
- Add comments to internships
- Validate or refuse submissions
- Track assigned students

### As Student (e.g., `alice.martin@student.ma` / `Password123!`)
- View personal internships
- Create new internship applications
- Check notifications
- Respond to instructor comments

---

## 🐳 Docker Commands

```bash
# Start all services
docker-compose -f docker-compose.local.yml up -d

# Check status
docker-compose -f docker-compose.local.yml ps

# View logs
docker logs internhub-backend
docker logs internhub-frontend

# Stop all services
docker-compose -f docker-compose.local.yml down

# Reset database (clear all data and let app reseed)
docker exec internhub-postgres psql -U postgres -d internhub -c "TRUNCATE notifications, comments, documents, internships, instructor_sectors, users, sectors RESTART IDENTITY CASCADE;"
docker restart internhub-backend
```
