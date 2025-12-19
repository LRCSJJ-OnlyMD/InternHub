-- ============================================
-- Seed Data for Internship Management System
-- ============================================
-- This script populates the database with test data
-- Run this after the application has created the schema

-- Clean existing data (optional - comment out if you want to keep existing data)
-- DELETE FROM internships;
-- DELETE FROM instructor_sectors;
-- DELETE FROM sectors;
-- DELETE FROM users WHERE role != 'ADMIN';

-- ============================================
-- 1. SECTORS (5+ entries)
-- ============================================
INSERT INTO sectors (name, code, description, created_at, updated_at) VALUES
('Information Technology', 'IT', 'Software development, web development, cybersecurity, databases, and IT infrastructure', NOW(), NOW()),
('Finance & Banking', 'FIN', 'Accounting, financial analysis, investment banking, and financial consulting', NOW(), NOW()),
('Marketing & Communication', 'MKT', 'Digital marketing, social media management, content creation, and brand management', NOW(), NOW()),
('Human Resources', 'HR', 'Recruitment, employee relations, training and development, and HR administration', NOW(), NOW()),
('Engineering & Manufacturing', 'ENG', 'Mechanical engineering, electrical engineering, production, and quality control', NOW(), NOW()),
('Healthcare & Pharmaceuticals', 'HEALTH', 'Medical research, pharmaceutical development, clinical trials, and healthcare administration', NOW(), NOW()),
('Legal & Compliance', 'LAW', 'Corporate law, contract management, regulatory compliance, and legal advisory', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- 2. USERS - Students (5 students)
-- ============================================
-- Password for all users: Password123! (BCrypt encoded)
INSERT INTO users (email, password, first_name, last_name, role, enabled, two_factor_enabled, created_at) VALUES
('alice.martin@student.ma', '$2a$10$cMs8jIuhzwNUv4RlyJmM6Om987y8qeQSK1lrDyO/YKHqFL/4twGwq', 'Alice', 'Martin', 'STUDENT', true, false, NOW()),
('bob.dupont@student.ma', '$2a$10$cMs8jIuhzwNUv4RlyJmM6Om987y8qeQSK1lrDyO/YKHqFL/4twGwq', 'Bob', 'Dupont', 'STUDENT', true, false, NOW()),
('claire.bernard@student.ma', '$2a$10$cMs8jIuhzwNUv4RlyJmM6Om987y8qeQSK1lrDyO/YKHqFL/4twGwq', 'Claire', 'Bernard', 'STUDENT', true, false, NOW()),
('david.rousseau@student.ma', '$2a$10$cMs8jIuhzwNUv4RlyJmM6Om987y8qeQSK1lrDyO/YKHqFL/4twGwq', 'David', 'Rousseau', 'STUDENT', true, false, NOW()),
('emma.petit@student.ma', '$2a$10$cMs8jIuhzwNUv4RlyJmM6Om987y8qeQSK1lrDyO/YKHqFL/4twGwq', 'Emma', 'Petit', 'STUDENT', true, false, NOW()),
('fatima.zahir@student.ma', '$2a$10$cMs8jIuhzwNUv4RlyJmM6Om987y8qeQSK1lrDyO/YKHqFL/4twGwq', 'Fatima', 'Zahir', 'STUDENT', true, false, NOW())
ON CONFLICT (email) DO NOTHING;

-- ============================================
-- 3. USERS - Instructors (5 instructors)
-- ============================================
INSERT INTO users (email, password, first_name, last_name, role, enabled, two_factor_enabled, created_at) VALUES
('prof.hassan@instructor.ma', '$2a$10$cMs8jIuhzwNUv4RlyJmM6Om987y8qeQSK1lrDyO/YKHqFL/4twGwq', 'Hassan', 'Alami', 'INSTRUCTOR', true, false, NOW()),
('prof.sarah@instructor.ma', '$2a$10$cMs8jIuhzwNUv4RlyJmM6Om987y8qeQSK1lrDyO/YKHqFL/4twGwq', 'Sarah', 'Bennani', 'INSTRUCTOR', true, false, NOW()),
('prof.omar@instructor.ma', '$2a$10$cMs8jIuhzwNUv4RlyJmM6Om987y8qeQSK1lrDyO/YKHqFL/4twGwq', 'Omar', 'Idrissi', 'INSTRUCTOR', true, false, NOW()),
('prof.latifa@instructor.ma', '$2a$10$cMs8jIuhzwNUv4RlyJmM6Om987y8qeQSK1lrDyO/YKHqFL/4twGwq', 'Latifa', 'Bouazza', 'INSTRUCTOR', true, false, NOW()),
('prof.karim@instructor.ma', '$2a$10$cMs8jIuhzwNUv4RlyJmM6Om987y8qeQSK1lrDyO/YKHqFL/4twGwq', 'Karim', 'Tazi', 'INSTRUCTOR', true, false, NOW()),
('prof.nadia@instructor.ma', '$2a$10$cMs8jIuhzwNUv4RlyJmM6Om987y8qeQSK1lrDyO/YKHqFL/4twGwq', 'Nadia', 'El Fassi', 'INSTRUCTOR', true, false, NOW())
ON CONFLICT (email) DO NOTHING;

-- ============================================
-- 4. INSTRUCTOR-SECTOR ASSIGNMENTS
-- ============================================
-- Assign instructors to sectors (each instructor can supervise multiple sectors)
INSERT INTO instructor_sectors (user_id, sector_id)
SELECT u.id, s.id FROM users u, sectors s 
WHERE u.email = 'prof.hassan@instructor.ma' AND s.code IN ('IT', 'ENG')
ON CONFLICT DO NOTHING;

INSERT INTO instructor_sectors (user_id, sector_id)
SELECT u.id, s.id FROM users u, sectors s 
WHERE u.email = 'prof.sarah@instructor.ma' AND s.code IN ('FIN', 'MKT')
ON CONFLICT DO NOTHING;

INSERT INTO instructor_sectors (user_id, sector_id)
SELECT u.id, s.id FROM users u, sectors s 
WHERE u.email = 'prof.omar@instructor.ma' AND s.code IN ('IT', 'HEALTH')
ON CONFLICT DO NOTHING;

INSERT INTO instructor_sectors (user_id, sector_id)
SELECT u.id, s.id FROM users u, sectors s 
WHERE u.email = 'prof.latifa@instructor.ma' AND s.code IN ('HR', 'LAW')
ON CONFLICT DO NOTHING;

INSERT INTO instructor_sectors (user_id, sector_id)
SELECT u.id, s.id FROM users u, sectors s 
WHERE u.email = 'prof.karim@instructor.ma' AND s.code IN ('ENG', 'HEALTH')
ON CONFLICT DO NOTHING;

INSERT INTO instructor_sectors (user_id, sector_id)
SELECT u.id, s.id FROM users u, sectors s 
WHERE u.email = 'prof.nadia@instructor.ma' AND s.code IN ('MKT', 'HR', 'LAW')
ON CONFLICT DO NOTHING;

-- ============================================
-- 5. INTERNSHIPS (15+ entries with various statuses)
-- ============================================

-- VALIDATED Internships (5)
INSERT INTO internships (title, description, company_name, company_address, start_date, end_date, status, student_id, instructor_id, sector_id, created_at, updated_at, submitted_at, validated_at)
SELECT 
    'Full Stack Web Development',
    'Development of a complete web application using Angular and Spring Boot. Implementation of REST APIs, authentication system, and responsive UI.',
    'TechCorp Morocco',
    '123 Boulevard Mohamed V, Casablanca',
    '2024-07-01',
    '2024-09-30',
    'VALIDATED',
    (SELECT id FROM users WHERE email = 'alice.martin@student.ma'),
    (SELECT id FROM users WHERE email = 'prof.hassan@instructor.ma'),
    (SELECT id FROM sectors WHERE code = 'IT'),
    NOW() - INTERVAL '60 days',
    NOW() - INTERVAL '45 days',
    NOW() - INTERVAL '55 days',
    NOW() - INTERVAL '45 days'
WHERE NOT EXISTS (SELECT 1 FROM internships WHERE title = 'Full Stack Web Development' AND company_name = 'TechCorp Morocco');

INSERT INTO internships (title, description, company_name, company_address, start_date, end_date, status, student_id, instructor_id, sector_id, created_at, updated_at, submitted_at, validated_at)
SELECT 
    'Financial Analysis & Reporting',
    'Analysis of financial statements, preparation of quarterly reports, and assistance in budget planning.',
    'Attijariwafa Bank',
    '2 Boulevard Moulay Youssef, Casablanca',
    '2024-06-15',
    '2024-08-31',
    'VALIDATED',
    (SELECT id FROM users WHERE email = 'bob.dupont@student.ma'),
    (SELECT id FROM users WHERE email = 'prof.sarah@instructor.ma'),
    (SELECT id FROM sectors WHERE code = 'FIN'),
    NOW() - INTERVAL '65 days',
    NOW() - INTERVAL '50 days',
    NOW() - INTERVAL '60 days',
    NOW() - INTERVAL '50 days'
WHERE NOT EXISTS (SELECT 1 FROM internships WHERE title = 'Financial Analysis & Reporting');

INSERT INTO internships (title, description, company_name, company_address, start_date, end_date, status, student_id, instructor_id, sector_id, created_at, updated_at, submitted_at, validated_at)
SELECT 
    'Digital Marketing Campaign',
    'Creation and management of social media campaigns, SEO optimization, and content marketing strategy.',
    'Majorel Morocco',
    'Casablanca Nearshore Park, Casablanca',
    '2024-07-10',
    '2024-10-10',
    'VALIDATED',
    (SELECT id FROM users WHERE email = 'claire.bernard@student.ma'),
    (SELECT id FROM users WHERE email = 'prof.nadia@instructor.ma'),
    (SELECT id FROM sectors WHERE code = 'MKT'),
    NOW() - INTERVAL '55 days',
    NOW() - INTERVAL '40 days',
    NOW() - INTERVAL '50 days',
    NOW() - INTERVAL '40 days'
WHERE NOT EXISTS (SELECT 1 FROM internships WHERE title = 'Digital Marketing Campaign');

INSERT INTO internships (title, description, company_name, company_address, start_date, end_date, status, student_id, instructor_id, sector_id, report_path, created_at, updated_at, submitted_at, validated_at)
SELECT 
    'Quality Control Engineering',
    'Implementation of quality control procedures, testing protocols, and ISO compliance documentation.',
    'Renault Maroc',
    'Zone Franche, Tanger',
    '2024-06-01',
    '2024-08-31',
    'VALIDATED',
    (SELECT id FROM users WHERE email = 'david.rousseau@student.ma'),
    (SELECT id FROM users WHERE email = 'prof.karim@instructor.ma'),
    (SELECT id FROM sectors WHERE code = 'ENG'),
    '/reports/david_renault_qc_report.pdf',
    NOW() - INTERVAL '70 days',
    NOW() - INTERVAL '35 days',
    NOW() - INTERVAL '65 days',
    NOW() - INTERVAL '35 days'
WHERE NOT EXISTS (SELECT 1 FROM internships WHERE title = 'Quality Control Engineering');

INSERT INTO internships (title, description, company_name, company_address, start_date, end_date, status, student_id, instructor_id, sector_id, report_path, created_at, updated_at, submitted_at, validated_at)
SELECT 
    'HR Recruitment Process',
    'Support in recruitment activities, candidate screening, interview coordination, and onboarding processes.',
    'Manpower Maroc',
    'Twin Center, Casablanca',
    '2024-07-15',
    '2024-10-15',
    'VALIDATED',
    (SELECT id FROM users WHERE email = 'emma.petit@student.ma'),
    (SELECT id FROM users WHERE email = 'prof.latifa@instructor.ma'),
    (SELECT id FROM sectors WHERE code = 'HR'),
    '/reports/emma_manpower_hr_report.pdf',
    NOW() - INTERVAL '50 days',
    NOW() - INTERVAL '30 days',
    NOW() - INTERVAL '45 days',
    NOW() - INTERVAL '30 days'
WHERE NOT EXISTS (SELECT 1 FROM internships WHERE title = 'HR Recruitment Process');

-- PENDING_VALIDATION Internships (5)
INSERT INTO internships (title, description, company_name, company_address, start_date, end_date, status, student_id, sector_id, created_at, updated_at, submitted_at)
SELECT 
    'Mobile App Development',
    'Development of a cross-platform mobile application using React Native. Implementation of user authentication and real-time data synchronization.',
    'Jumia Morocco',
    'Marina Shopping Center, Casablanca',
    '2024-09-01',
    '2024-11-30',
    'PENDING_VALIDATION',
    (SELECT id FROM users WHERE email = 'fatima.zahir@student.ma'),
    (SELECT id FROM sectors WHERE code = 'IT'),
    NOW() - INTERVAL '10 days',
    NOW() - INTERVAL '5 days',
    NOW() - INTERVAL '5 days'
WHERE NOT EXISTS (SELECT 1 FROM internships WHERE title = 'Mobile App Development');

INSERT INTO internships (title, description, company_name, company_address, start_date, end_date, status, student_id, sector_id, created_at, updated_at, submitted_at)
SELECT 
    'Investment Banking Analysis',
    'Financial modeling, market research, and support in M&A transactions.',
    'CFG Bank',
    'Boulevard Moulay Slimane, Casablanca',
    '2024-09-15',
    '2024-12-15',
    'PENDING_VALIDATION',
    (SELECT id FROM users WHERE email = 'alice.martin@student.ma'),
    (SELECT id FROM sectors WHERE code = 'FIN'),
    NOW() - INTERVAL '8 days',
    NOW() - INTERVAL '3 days',
    NOW() - INTERVAL '3 days'
WHERE NOT EXISTS (SELECT 1 FROM internships WHERE title = 'Investment Banking Analysis');

INSERT INTO internships (title, description, company_name, company_address, start_date, end_date, status, student_id, sector_id, created_at, updated_at, submitted_at)
SELECT 
    'Pharmaceutical Research Assistant',
    'Laboratory work, clinical trial data collection, and research documentation.',
    'Sanofi Maroc',
    'Technopolis, Rabat',
    '2024-08-20',
    '2024-11-20',
    'PENDING_VALIDATION',
    (SELECT id FROM users WHERE email = 'bob.dupont@student.ma'),
    (SELECT id FROM sectors WHERE code = 'HEALTH'),
    NOW() - INTERVAL '12 days',
    NOW() - INTERVAL '4 days',
    NOW() - INTERVAL '4 days'
WHERE NOT EXISTS (SELECT 1 FROM internships WHERE title = 'Pharmaceutical Research Assistant');

INSERT INTO internships (title, description, company_name, company_address, start_date, end_date, status, student_id, sector_id, created_at, updated_at, submitted_at)
SELECT 
    'Corporate Legal Support',
    'Contract review, legal research, compliance documentation, and corporate governance support.',
    'Cabinet d''Avocats Bennani',
    'Avenue Hassan II, Rabat',
    '2024-09-10',
    '2024-12-10',
    'PENDING_VALIDATION',
    (SELECT id FROM users WHERE email = 'claire.bernard@student.ma'),
    (SELECT id FROM sectors WHERE code = 'LAW'),
    NOW() - INTERVAL '6 days',
    NOW() - INTERVAL '2 days',
    NOW() - INTERVAL '2 days'
WHERE NOT EXISTS (SELECT 1 FROM internships WHERE title = 'Corporate Legal Support');

INSERT INTO internships (title, description, company_name, company_address, start_date, end_date, status, student_id, sector_id, created_at, updated_at, submitted_at)
SELECT 
    'Brand Management Internship',
    'Brand strategy development, market positioning analysis, and advertising campaign coordination.',
    'Maroc Telecom',
    'Avenue Annakhil, Rabat',
    '2024-08-25',
    '2024-11-25',
    'PENDING_VALIDATION',
    (SELECT id FROM users WHERE email = 'david.rousseau@student.ma'),
    (SELECT id FROM sectors WHERE code = 'MKT'),
    NOW() - INTERVAL '7 days',
    NOW() - INTERVAL '1 day',
    NOW() - INTERVAL '1 day'
WHERE NOT EXISTS (SELECT 1 FROM internships WHERE title = 'Brand Management Internship');

-- REFUSED Internships (3)
INSERT INTO internships (title, description, company_name, company_address, start_date, end_date, status, student_id, instructor_id, sector_id, refusal_comment, created_at, updated_at, submitted_at)
SELECT 
    'Basic Data Entry',
    'Simple data entry tasks and file organization.',
    'Small Local Company',
    'Somewhere in Morocco',
    '2024-08-01',
    '2024-08-31',
    'REFUSED',
    (SELECT id FROM users WHERE email = 'emma.petit@student.ma'),
    (SELECT id FROM users WHERE email = 'prof.hassan@instructor.ma'),
    (SELECT id FROM sectors WHERE code = 'IT'),
    'The internship does not meet the educational requirements. Tasks are too basic and do not align with IT program learning objectives. Please look for an internship with more technical responsibilities.',
    NOW() - INTERVAL '40 days',
    NOW() - INTERVAL '30 days',
    NOW() - INTERVAL '35 days'
WHERE NOT EXISTS (SELECT 1 FROM internships WHERE title = 'Basic Data Entry');

INSERT INTO internships (title, description, company_name, company_address, start_date, end_date, status, student_id, instructor_id, sector_id, refusal_comment, created_at, updated_at, submitted_at)
SELECT 
    'Office Assistant Role',
    'General office support and administrative tasks.',
    'Generic Office LLC',
    'Downtown, Casablanca',
    '2024-07-20',
    '2024-08-20',
    'REFUSED',
    (SELECT id FROM users WHERE email = 'fatima.zahir@student.ma'),
    (SELECT id FROM users WHERE email = 'prof.sarah@instructor.ma'),
    (SELECT id FROM sectors WHERE code = 'FIN'),
    'Duration is too short (only 1 month). Minimum required duration is 3 months. Additionally, the role description lacks specific finance-related responsibilities.',
    NOW() - INTERVAL '45 days',
    NOW() - INTERVAL '35 days',
    NOW() - INTERVAL '40 days'
WHERE NOT EXISTS (SELECT 1 FROM internships WHERE title = 'Office Assistant Role');

INSERT INTO internships (title, description, company_name, company_address, start_date, end_date, status, student_id, instructor_id, sector_id, refusal_comment, created_at, updated_at, submitted_at)
SELECT 
    'Unpaid Sales Position',
    'Sales and customer service with no compensation.',
    'Retail Store',
    'Local Market, Marrakech',
    '2024-06-10',
    '2024-09-10',
    'REFUSED',
    (SELECT id FROM users WHERE email = 'alice.martin@student.ma'),
    (SELECT id FROM users WHERE email = 'prof.nadia@instructor.ma'),
    (SELECT id FROM sectors WHERE code = 'MKT'),
    'This position does not comply with internship regulations. The role appears to be a regular sales position rather than a structured learning experience. Please seek opportunities with proper mentorship and educational objectives.',
    NOW() - INTERVAL '50 days',
    NOW() - INTERVAL '40 days',
    NOW() - INTERVAL '48 days'
WHERE NOT EXISTS (SELECT 1 FROM internships WHERE title = 'Unpaid Sales Position');

-- DRAFT Internships (3)
INSERT INTO internships (title, description, company_name, company_address, start_date, end_date, status, student_id, sector_id, created_at, updated_at)
SELECT 
    'Cybersecurity Analysis',
    'Network security assessment, vulnerability testing, and security documentation.',
    'OCP Group',
    'Hay Erraha, Casablanca',
    '2024-10-01',
    '2024-12-31',
    'DRAFT',
    (SELECT id FROM users WHERE email = 'bob.dupont@student.ma'),
    (SELECT id FROM sectors WHERE code = 'IT'),
    NOW() - INTERVAL '5 days',
    NOW() - INTERVAL '1 day'
WHERE NOT EXISTS (SELECT 1 FROM internships WHERE title = 'Cybersecurity Analysis');

INSERT INTO internships (title, description, company_name, company_address, start_date, end_date, status, student_id, sector_id, created_at, updated_at)
SELECT 
    'Employee Training Program Development',
    'Design and implementation of employee training modules and performance evaluation systems.',
    'ONCF',
    'Avenue Mohamed V, Rabat',
    '2024-10-15',
    '2025-01-15',
    'DRAFT',
    (SELECT id FROM users WHERE email = 'claire.bernard@student.ma'),
    (SELECT id FROM sectors WHERE code = 'HR'),
    NOW() - INTERVAL '3 days',
    NOW() - INTERVAL '1 day'
WHERE NOT EXISTS (SELECT 1 FROM internships WHERE title = 'Employee Training Program Development');

INSERT INTO internships (title, description, company_name, company_address, start_date, end_date, status, student_id, sector_id, created_at, updated_at)
SELECT 
    'Automotive Production Engineering',
    '',
    'PSA Peugeot Citroën',
    'Zone Industrielle, Kenitra',
    '2024-11-01',
    '2025-02-01',
    'DRAFT',
    (SELECT id FROM users WHERE email = 'david.rousseau@student.ma'),
    (SELECT id FROM sectors WHERE code = 'ENG'),
    NOW() - INTERVAL '2 days',
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM internships WHERE title = 'Automotive Production Engineering');

-- ============================================
-- SUMMARY
-- ============================================
-- Total Records Created:
-- - 7 Sectors
-- - 6 Students
-- - 6 Instructors
-- - 6 Instructor-Sector assignments
-- - 16 Internships:
--   * 5 VALIDATED (with instructors assigned)
--   * 5 PENDING_VALIDATION (waiting for instructor review)
--   * 3 REFUSED (with refusal comments)
--   * 3 DRAFT (not yet submitted)
--
-- Login Credentials (all users):
-- Email: <user_email_from_above>
-- Password: Password123!
--
-- Admin account should already exist:
-- Email: admin@internship.com
-- Password: Admin123!
-- ============================================
