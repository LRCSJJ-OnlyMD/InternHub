-- InternHub Mock Data Initialization Script
-- Run this after the application has created the tables

-- Clear existing data (in correct order due to foreign keys)
DELETE FROM notifications;
DELETE FROM comments;
DELETE FROM documents;
DELETE FROM internships;
DELETE FROM instructor_sectors;
DELETE FROM users;
DELETE FROM sectors;

-- Reset sequences
ALTER SEQUENCE IF EXISTS users_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS sectors_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS internships_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS comments_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS notifications_id_seq RESTART WITH 1;

-- =====================================================
-- SECTORS
-- =====================================================
INSERT INTO sectors (id, name, code, description, created_at, updated_at) VALUES
(1, 'Information Technology', 'IT', 'Software development, IT infrastructure, and digital solutions', NOW(), NOW()),
(2, 'Finance & Banking', 'FIN', 'Banking, investment, and financial services', NOW(), NOW()),
(3, 'Healthcare', 'HEALTH', 'Medical, pharmaceutical, and healthcare services', NOW(), NOW()),
(4, 'Marketing & Communications', 'MKT', 'Digital marketing, advertising, and public relations', NOW(), NOW()),
(5, 'Engineering', 'ENG', 'Mechanical, electrical, and civil engineering', NOW(), NOW()),
(6, 'Human Resources', 'HR', 'Recruitment, training, and employee management', NOW(), NOW()),
(7, 'Data Science & AI', 'DATA', 'Data analytics, machine learning, and artificial intelligence', NOW(), NOW()),
(8, 'Cybersecurity', 'SEC', 'Information security and risk management', NOW(), NOW());

-- =====================================================
-- USERS
-- Password is 'Password123!' hashed with BCrypt
-- =====================================================
-- Admin User (your account)
INSERT INTO users (id, email, password, first_name, last_name, department, role, enabled, two_factor_enabled, created_at, account_activated, must_change_password) VALUES
(1, 'mouadom2003@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGPOEqNP1sV6Sz6S7o7M5pjRVGIC', 'Mouad', 'Admin', 'Administration', 'ADMIN', true, false, NOW(), true, false);

-- Instructors
INSERT INTO users (id, email, password, first_name, last_name, department, role, enabled, two_factor_enabled, created_at, account_activated, must_change_password) VALUES
(2, 'jean.dupont@university.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGPOEqNP1sV6Sz6S7o7M5pjRVGIC', 'Jean', 'Dupont', 'Computer Science', 'INSTRUCTOR', true, false, NOW(), true, false),
(3, 'marie.laurent@university.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGPOEqNP1sV6Sz6S7o7M5pjRVGIC', 'Marie', 'Laurent', 'Business Administration', 'INSTRUCTOR', true, false, NOW(), true, false),
(4, 'pierre.martin@university.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGPOEqNP1sV6Sz6S7o7M5pjRVGIC', 'Pierre', 'Martin', 'Engineering', 'INSTRUCTOR', true, false, NOW(), true, false),
(5, 'sophie.bernard@university.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGPOEqNP1sV6Sz6S7o7M5pjRVGIC', 'Sophie', 'Bernard', 'Data Science', 'INSTRUCTOR', true, false, NOW(), true, false);

-- Students
INSERT INTO users (id, email, password, first_name, last_name, department, role, enabled, two_factor_enabled, created_at, account_activated, must_change_password) VALUES
(6, 'alice.johnson@student.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGPOEqNP1sV6Sz6S7o7M5pjRVGIC', 'Alice', 'Johnson', 'Computer Science', 'STUDENT', true, false, NOW(), true, false),
(7, 'bob.smith@student.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGPOEqNP1sV6Sz6S7o7M5pjRVGIC', 'Bob', 'Smith', 'Computer Science', 'STUDENT', true, false, NOW(), true, false),
(8, 'claire.dubois@student.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGPOEqNP1sV6Sz6S7o7M5pjRVGIC', 'Claire', 'Dubois', 'Business Administration', 'STUDENT', true, false, NOW(), true, false),
(9, 'david.wilson@student.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGPOEqNP1sV6Sz6S7o7M5pjRVGIC', 'David', 'Wilson', 'Engineering', 'STUDENT', true, false, NOW(), true, false),
(10, 'emma.garcia@student.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGPOEqNP1sV6Sz6S7o7M5pjRVGIC', 'Emma', 'Garcia', 'Data Science', 'STUDENT', true, false, NOW(), true, false),
(11, 'frank.miller@student.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGPOEqNP1sV6Sz6S7o7M5pjRVGIC', 'Frank', 'Miller', 'Computer Science', 'STUDENT', true, false, NOW(), true, false),
(12, 'grace.lee@student.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGPOEqNP1sV6Sz6S7o7M5pjRVGIC', 'Grace', 'Lee', 'Business Administration', 'STUDENT', true, false, NOW(), true, false),
(13, 'henry.brown@student.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGPOEqNP1sV6Sz6S7o7M5pjRVGIC', 'Henry', 'Brown', 'Engineering', 'STUDENT', true, false, NOW(), true, false),
(14, 'isabel.martinez@student.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGPOEqNP1sV6Sz6S7o7M5pjRVGIC', 'Isabel', 'Martinez', 'Data Science', 'STUDENT', true, false, NOW(), true, false),
(15, 'jack.taylor@student.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGPOEqNP1sV6Sz6S7o7M5pjRVGIC', 'Jack', 'Taylor', 'Computer Science', 'STUDENT', true, false, NOW(), true, false);

-- Update sequence to continue after our inserts
SELECT setval('users_id_seq', 15);
SELECT setval('sectors_id_seq', 8);

-- =====================================================
-- INSTRUCTOR SECTORS (Assign sectors to instructors)
-- =====================================================
INSERT INTO instructor_sectors (user_id, sector_id) VALUES
(2, 1), -- Jean Dupont -> IT
(2, 7), -- Jean Dupont -> Data Science
(2, 8), -- Jean Dupont -> Cybersecurity
(3, 2), -- Marie Laurent -> Finance
(3, 4), -- Marie Laurent -> Marketing
(3, 6), -- Marie Laurent -> HR
(4, 5), -- Pierre Martin -> Engineering
(4, 1), -- Pierre Martin -> IT
(5, 7), -- Sophie Bernard -> Data Science
(5, 1); -- Sophie Bernard -> IT

-- =====================================================
-- INTERNSHIPS (Various statuses)
-- =====================================================

-- COMPLETED Internships
INSERT INTO internships (id, title, description, company_name, company_address, start_date, end_date, status, student_id, instructor_id, sector_id, created_at, updated_at, submitted_at) VALUES
(1, 'Full Stack Developer Internship', 'Developed web applications using React and Node.js. Implemented REST APIs and database optimization.', 'TechCorp Solutions', '123 Silicon Valley, San Francisco, CA', '2025-01-15', '2025-06-15', 'COMPLETED', 6, 2, 1, NOW() - INTERVAL '8 months', NOW(), NOW() - INTERVAL '8 months'),
(2, 'Data Analytics Project', 'Analyzed customer behavior data using Python and SQL. Created dashboards with Tableau.', 'DataViz Inc', '456 Analytics Blvd, New York, NY', '2025-02-01', '2025-07-01', 'COMPLETED', 10, 5, 7, NOW() - INTERVAL '7 months', NOW(), NOW() - INTERVAL '7 months'),
(3, 'Financial Analysis Internship', 'Assisted in quarterly financial reports and budget forecasting for major clients.', 'Goldman Finance', '789 Wall Street, New York, NY', '2025-03-01', '2025-08-01', 'COMPLETED', 8, 3, 2, NOW() - INTERVAL '6 months', NOW(), NOW() - INTERVAL '6 months');

-- IN_PROGRESS Internships
INSERT INTO internships (id, title, description, company_name, company_address, start_date, end_date, status, student_id, instructor_id, sector_id, created_at, updated_at, submitted_at) VALUES
(4, 'Machine Learning Engineer Intern', 'Working on NLP models for sentiment analysis. Using TensorFlow and PyTorch.', 'AI Dynamics', '321 Innovation Park, Boston, MA', '2025-09-01', '2026-02-28', 'IN_PROGRESS', 14, 5, 7, NOW() - INTERVAL '4 months', NOW(), NOW() - INTERVAL '4 months'),
(5, 'Cybersecurity Analyst Intern', 'Performing vulnerability assessments and penetration testing. Learning incident response.', 'SecureNet Corp', '555 Security Lane, Austin, TX', '2025-10-01', '2026-03-31', 'IN_PROGRESS', 7, 2, 8, NOW() - INTERVAL '3 months', NOW(), NOW() - INTERVAL '3 months'),
(6, 'Marketing Campaign Manager', 'Managing social media campaigns and analyzing engagement metrics for brand awareness.', 'BrandBoost Agency', '777 Marketing Ave, Los Angeles, CA', '2025-11-01', '2026-04-30', 'IN_PROGRESS', 12, 3, 4, NOW() - INTERVAL '2 months', NOW(), NOW() - INTERVAL '2 months');

-- VALIDATED Internships (approved, not yet started)
INSERT INTO internships (id, title, description, company_name, company_address, start_date, end_date, status, student_id, instructor_id, sector_id, created_at, updated_at, submitted_at) VALUES
(7, 'Backend Developer Internship', 'Will work on microservices architecture using Spring Boot and Kubernetes.', 'CloudScale Systems', '888 Cloud Computing Dr, Seattle, WA', '2026-02-01', '2026-07-31', 'VALIDATED', 11, 2, 1, NOW() - INTERVAL '1 month', NOW(), NOW() - INTERVAL '1 month'),
(8, 'Civil Engineering Project', 'Infrastructure design and project management for sustainable building projects.', 'GreenBuild Engineers', '999 Construction Blvd, Denver, CO', '2026-03-01', '2026-08-31', 'VALIDATED', 9, 4, 5, NOW() - INTERVAL '2 weeks', NOW(), NOW() - INTERVAL '2 weeks');

-- PENDING_VALIDATION Internships
INSERT INTO internships (id, title, description, company_name, company_address, start_date, end_date, status, student_id, sector_id, created_at, updated_at, submitted_at) VALUES
(9, 'DevOps Engineer Intern', 'Learning CI/CD pipelines, Docker, and cloud deployment on AWS.', 'CloudOps Tech', '111 DevOps Street, Portland, OR', '2026-04-01', '2026-09-30', 'PENDING_VALIDATION', 15, 1, NOW() - INTERVAL '5 days', NOW(), NOW() - INTERVAL '5 days'),
(10, 'HR Analytics Internship', 'Applying data analytics to HR processes and employee engagement metrics.', 'PeopleFirst HR', '222 Human Resources Way, Chicago, IL', '2026-05-01', '2026-10-31', 'PENDING_VALIDATION', 12, 6, NOW() - INTERVAL '3 days', NOW(), NOW() - INTERVAL '3 days'),
(11, 'Mobile App Developer', 'Developing cross-platform mobile applications using Flutter and React Native.', 'AppWorks Studio', '333 Mobile Dev Lane, San Diego, CA', '2026-04-15', '2026-10-15', 'PENDING_VALIDATION', 6, 1, NOW() - INTERVAL '2 days', NOW(), NOW() - INTERVAL '2 days');

-- REFUSED Internships
INSERT INTO internships (id, title, description, company_name, company_address, start_date, end_date, status, student_id, instructor_id, sector_id, refusal_comment, created_at, updated_at, submitted_at) VALUES
(12, 'Generic Office Work', 'Basic administrative tasks and data entry.', 'Random Office Inc', '444 Boring Street, Somewhere, USA', '2026-03-01', '2026-06-01', 'REFUSED', 13, 4, 5, 'This internship does not meet the academic requirements. The tasks described are not related to your field of study and lack technical depth.', NOW() - INTERVAL '1 month', NOW(), NOW() - INTERVAL '1 month');

-- DRAFT Internships
INSERT INTO internships (id, title, description, company_name, company_address, start_date, end_date, status, student_id, sector_id, created_at, updated_at) VALUES
(13, 'Software Testing Internship', 'Learning automated testing with Selenium and Jest.', 'QualityFirst Software', '555 Testing Ave, Raleigh, NC', '2026-06-01', '2026-11-30', 'DRAFT', 7, 1, NOW() - INTERVAL '1 day', NOW()),
(14, 'Investment Banking Analyst', 'Financial modeling and market research for M&A transactions.', 'Morgan Investments', '666 Finance District, New York, NY', '2026-07-01', '2026-12-31', 'DRAFT', 8, 2, NOW(), NOW());

-- Update sequence
SELECT setval('internships_id_seq', 14);

-- =====================================================
-- COMMENTS
-- =====================================================
INSERT INTO comments (id, internship_id, user_id, content, created_at, is_edited) VALUES
-- Comments on completed internship 1
(1, 1, 2, 'Excellent work on the REST API implementation. Your code is clean and well-documented.', NOW() - INTERVAL '6 months', false),
(2, 1, 6, 'Thank you! I learned a lot about API design patterns during this internship.', NOW() - INTERVAL '6 months' + INTERVAL '1 day', false),
(3, 1, 2, 'Final evaluation submitted. Outstanding performance overall.', NOW() - INTERVAL '5 months', false),

-- Comments on in-progress internship 4
(4, 4, 5, 'Great progress on the NLP model. The accuracy metrics are impressive.', NOW() - INTERVAL '2 months', false),
(5, 4, 14, 'Working on improving the model with transformer architecture now.', NOW() - INTERVAL '1 month', false),
(6, 4, 5, 'Please submit your mid-term report by the end of this week.', NOW() - INTERVAL '2 weeks', false),

-- Comments on in-progress internship 5
(7, 5, 2, 'Your vulnerability assessment report was thorough. Good job identifying the SQL injection risks.', NOW() - INTERVAL '1 month', false),
(8, 5, 7, 'Thank you! Im now focusing on the penetration testing phase.', NOW() - INTERVAL '3 weeks', false),

-- Comments on pending validation internship 9
(9, 9, 15, 'I have updated the description to include more technical details about the CI/CD work.', NOW() - INTERVAL '4 days', false),

-- Comments on refused internship 12
(10, 12, 4, 'Please find an internship that aligns better with your engineering curriculum.', NOW() - INTERVAL '1 month', false),
(11, 12, 13, 'I understand. I will look for a more relevant opportunity.', NOW() - INTERVAL '1 month' + INTERVAL '1 day', false);

-- Update sequence
SELECT setval('comments_id_seq', 11);

-- =====================================================
-- NOTIFICATIONS
-- =====================================================
INSERT INTO notifications (id, user_id, type, title, message, entity_type, entity_id, is_read, created_at) VALUES
-- Admin notifications
(1, 1, 'USER_ACTION', 'New User Registration', 'A new student Jack Taylor has registered.', 'USER', 15, true, NOW() - INTERVAL '5 days'),
(2, 1, 'USER_ACTION', 'New Instructor Added', 'Instructor Sophie Bernard has been added to the system.', 'USER', 5, true, NOW() - INTERVAL '2 weeks'),

-- Instructor notifications
(3, 2, 'INTERNSHIP_STATUS', 'New Internship Submitted', 'Alice Johnson has submitted a new internship for validation: Mobile App Developer', 'INTERNSHIP', 11, false, NOW() - INTERVAL '2 days'),
(4, 2, 'INTERNSHIP_STATUS', 'New Internship Submitted', 'Jack Taylor has submitted a new internship for validation: DevOps Engineer Intern', 'INTERNSHIP', 9, false, NOW() - INTERVAL '5 days'),
(5, 3, 'INTERNSHIP_STATUS', 'New Internship Submitted', 'Grace Lee has submitted a new internship for validation: HR Analytics Internship', 'INTERNSHIP', 10, false, NOW() - INTERVAL '3 days'),
(6, 5, 'COMMENT', 'New Comment', 'Isabel Martinez commented on Machine Learning Engineer Intern', 'INTERNSHIP', 4, true, NOW() - INTERVAL '1 month'),

-- Student notifications
(7, 6, 'VALIDATE', 'Internship Approved', 'Your internship "Full Stack Developer Internship" has been approved!', 'INTERNSHIP', 1, true, NOW() - INTERVAL '8 months'),
(8, 7, 'VALIDATE', 'Internship Approved', 'Your internship "Cybersecurity Analyst Intern" has been approved and is now in progress.', 'INTERNSHIP', 5, true, NOW() - INTERVAL '3 months'),
(9, 11, 'VALIDATE', 'Internship Approved', 'Your internship "Backend Developer Internship" has been validated!', 'INTERNSHIP', 7, true, NOW() - INTERVAL '1 month'),
(10, 13, 'REFUSE', 'Internship Refused', 'Your internship "Generic Office Work" has been refused. Please check the comments for details.', 'INTERNSHIP', 12, true, NOW() - INTERVAL '1 month'),
(11, 14, 'COMMENT', 'New Comment from Instructor', 'Sophie Bernard commented on your internship "Machine Learning Engineer Intern"', 'INTERNSHIP', 4, false, NOW() - INTERVAL '2 weeks'),
(12, 15, 'INTERNSHIP_STATUS', 'Internship Pending Review', 'Your internship "DevOps Engineer Intern" is now pending validation.', 'INTERNSHIP', 9, true, NOW() - INTERVAL '5 days');

-- Update sequence
SELECT setval('notifications_id_seq', 12);

-- =====================================================
-- SUMMARY
-- =====================================================
-- Admin: mouadom2003@gmail.com / Password123!
-- All other users: [email] / Password123!
--
-- Users Created:
--   1 Admin
--   4 Instructors
--   10 Students
--
-- Internships Created:
--   3 COMPLETED
--   3 IN_PROGRESS
--   2 VALIDATED
--   3 PENDING_VALIDATION
--   1 REFUSED
--   2 DRAFT
--
-- Total: 14 internships, 11 comments, 12 notifications
