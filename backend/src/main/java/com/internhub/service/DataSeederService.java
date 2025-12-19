package com.internhub.service;

import com.internhub.model.*;
import com.internhub.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Seeds the database with initial test data including: - Sectors - Students and
 * Instructors - Instructor-Sector assignments - Sample internships with various
 * statuses
 *
 * This seeder runs only if the database is empty (no students found).
 */
@Component
@Order(2)  // Run after AdminInitializerService
public class DataSeederService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SectorRepository sectorRepository;
    private final InternshipRepository internshipRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeederService(
            UserRepository userRepository,
            SectorRepository sectorRepository,
            InternshipRepository internshipRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sectorRepository = sectorRepository;
        this.internshipRepository = internshipRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Check if data already exists (look for students)
        boolean dataExists = userRepository.findAll().stream()
                .anyMatch(user -> user.getRole() == Role.STUDENT);

        if (!dataExists) {
            System.out.println("========================================");
            System.out.println("SEEDING DATABASE WITH TEST DATA...");
            System.out.println("========================================");

            seedSectors();
            seedStudents();
            seedInstructors();
            seedInternships();

            System.out.println("========================================");
            System.out.println("DATABASE SEEDING COMPLETED");
            System.out.println("========================================");
            System.out.println("Test Accounts Created:");
            System.out.println("- 6 Students (alice.martin@student.ma, bob.dupont@student.ma, etc.)");
            System.out.println("- 6 Instructors (prof.hassan@instructor.ma, prof.sarah@instructor.ma, etc.)");
            System.out.println("- 7 Sectors");
            System.out.println("- 16 Internships (5 validated, 5 pending, 3 refused, 3 drafts)");
            System.out.println("All passwords: Password123!");
            System.out.println("========================================");
        }
    }

    private void seedSectors() {
        List<Sector> sectors = Arrays.asList(
                createSector("Information Technology", "IT", "Software development, web development, cybersecurity, databases, and IT infrastructure"),
                createSector("Finance & Banking", "FIN", "Accounting, financial analysis, investment banking, and financial consulting"),
                createSector("Marketing & Communication", "MKT", "Digital marketing, social media management, content creation, and brand management"),
                createSector("Human Resources", "HR", "Recruitment, employee relations, training and development, and HR administration"),
                createSector("Engineering & Manufacturing", "ENG", "Mechanical engineering, electrical engineering, production, and quality control"),
                createSector("Healthcare & Pharmaceuticals", "HEALTH", "Medical research, pharmaceutical development, clinical trials, and healthcare administration"),
                createSector("Legal & Compliance", "LAW", "Corporate law, contract management, regulatory compliance, and legal advisory")
        );

        for (Sector sector : sectors) {
            if (sectorRepository.findByCode(sector.getCode()).isEmpty()) {
                sectorRepository.save(sector);
            }
        }
        System.out.println("✓ Sectors seeded");
    }

    private Sector createSector(String name, String code, String description) {
        Sector sector = new Sector();
        sector.setName(name);
        sector.setCode(code);
        sector.setDescription(description);
        sector.setCreatedAt(LocalDateTime.now());
        sector.setUpdatedAt(LocalDateTime.now());
        return sector;
    }

    private void seedStudents() {
        String encodedPassword = passwordEncoder.encode("Password123!");

        List<User> students = Arrays.asList(
                createUser("alice.martin@student.ma", "Alice", "Martin", Role.STUDENT, encodedPassword),
                createUser("bob.dupont@student.ma", "Bob", "Dupont", Role.STUDENT, encodedPassword),
                createUser("claire.bernard@student.ma", "Claire", "Bernard", Role.STUDENT, encodedPassword),
                createUser("david.rousseau@student.ma", "David", "Rousseau", Role.STUDENT, encodedPassword),
                createUser("emma.petit@student.ma", "Emma", "Petit", Role.STUDENT, encodedPassword),
                createUser("fatima.zahir@student.ma", "Fatima", "Zahir", Role.STUDENT, encodedPassword)
        );

        for (User student : students) {
            if (userRepository.findByEmail(student.getEmail()).isEmpty()) {
                userRepository.save(student);
            }
        }
        System.out.println("✓ Students seeded");
    }

    private void seedInstructors() {
        String encodedPassword = passwordEncoder.encode("Password123!");

        // Create instructors
        User hassan = createUser("prof.hassan@instructor.ma", "Hassan", "Alami", Role.INSTRUCTOR, encodedPassword);
        User sarah = createUser("prof.sarah@instructor.ma", "Sarah", "Bennani", Role.INSTRUCTOR, encodedPassword);
        User omar = createUser("prof.omar@instructor.ma", "Omar", "Idrissi", Role.INSTRUCTOR, encodedPassword);
        User latifa = createUser("prof.latifa@instructor.ma", "Latifa", "Bouazza", Role.INSTRUCTOR, encodedPassword);
        User karim = createUser("prof.karim@instructor.ma", "Karim", "Tazi", Role.INSTRUCTOR, encodedPassword);
        User nadia = createUser("prof.nadia@instructor.ma", "Nadia", "El Fassi", Role.INSTRUCTOR, encodedPassword);

        // Assign sectors
        assignSectorsToInstructor(hassan, "IT", "ENG");
        assignSectorsToInstructor(sarah, "FIN", "MKT");
        assignSectorsToInstructor(omar, "IT", "HEALTH");
        assignSectorsToInstructor(latifa, "HR", "LAW");
        assignSectorsToInstructor(karim, "ENG", "HEALTH");
        assignSectorsToInstructor(nadia, "MKT", "HR", "LAW");

        // Save instructors
        List<User> instructors = Arrays.asList(hassan, sarah, omar, latifa, karim, nadia);
        for (User instructor : instructors) {
            if (userRepository.findByEmail(instructor.getEmail()).isEmpty()) {
                userRepository.save(instructor);
            }
        }
        System.out.println("✓ Instructors seeded with sector assignments");
    }

    private User createUser(String email, String firstName, String lastName, Role role, String encodedPassword) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setEnabled(true);
        user.setTwoFactorEnabled(false);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private void assignSectorsToInstructor(User instructor, String... sectorCodes) {
        Set<Sector> sectors = new HashSet<>();
        for (String code : sectorCodes) {
            sectorRepository.findByCode(code).ifPresent(sectors::add);
        }
        instructor.setSectors(sectors);
    }

    private void seedInternships() {
        // Get users
        User alice = userRepository.findByEmail("alice.martin@student.ma").orElse(null);
        User bob = userRepository.findByEmail("bob.dupont@student.ma").orElse(null);
        User claire = userRepository.findByEmail("claire.bernard@student.ma").orElse(null);
        User david = userRepository.findByEmail("david.rousseau@student.ma").orElse(null);
        User emma = userRepository.findByEmail("emma.petit@student.ma").orElse(null);
        User fatima = userRepository.findByEmail("fatima.zahir@student.ma").orElse(null);

        User hassan = userRepository.findByEmail("prof.hassan@instructor.ma").orElse(null);
        User sarah = userRepository.findByEmail("prof.sarah@instructor.ma").orElse(null);
        User omar = userRepository.findByEmail("prof.omar@instructor.ma").orElse(null);
        User latifa = userRepository.findByEmail("prof.latifa@instructor.ma").orElse(null);
        User karim = userRepository.findByEmail("prof.karim@instructor.ma").orElse(null);
        User nadia = userRepository.findByEmail("prof.nadia@instructor.ma").orElse(null);

        // Get sectors
        Sector it = sectorRepository.findByCode("IT").orElse(null);
        Sector fin = sectorRepository.findByCode("FIN").orElse(null);
        Sector mkt = sectorRepository.findByCode("MKT").orElse(null);
        Sector hr = sectorRepository.findByCode("HR").orElse(null);
        Sector eng = sectorRepository.findByCode("ENG").orElse(null);
        Sector health = sectorRepository.findByCode("HEALTH").orElse(null);
        Sector law = sectorRepository.findByCode("LAW").orElse(null);

        List<Internship> internships = new ArrayList<>();

        // VALIDATED Internships (5)
        internships.add(createValidatedInternship(
                "Full Stack Web Development",
                "Development of a complete web application using Angular and Spring Boot. Implementation of REST APIs, authentication system, and responsive UI.",
                "TechCorp Morocco", "123 Boulevard Mohamed V, Casablanca",
                LocalDate.now().minusDays(90), LocalDate.now().minusDays(0),
                alice, hassan, it, 60, 45
        ));

        internships.add(createValidatedInternship(
                "Financial Analysis & Reporting",
                "Analysis of financial statements, preparation of quarterly reports, and assistance in budget planning.",
                "Attijariwafa Bank", "2 Boulevard Moulay Youssef, Casablanca",
                LocalDate.now().minusDays(107), LocalDate.now().minusDays(31),
                bob, sarah, fin, 65, 50
        ));

        internships.add(createValidatedInternship(
                "Digital Marketing Campaign",
                "Creation and management of social media campaigns, SEO optimization, and content marketing strategy.",
                "Majorel Morocco", "Casablanca Nearshore Park, Casablanca",
                LocalDate.now().minusDays(79), LocalDate.now().plusDays(13),
                claire, nadia, mkt, 55, 40
        ));

        internships.add(createValidatedInternship(
                "Quality Control Engineering",
                "Implementation of quality control procedures, testing protocols, and ISO compliance documentation.",
                "Renault Maroc", "Zone Franche, Tanger",
                LocalDate.now().minusDays(122), LocalDate.now().minusDays(31),
                david, karim, eng, 70, 35
        ));

        internships.add(createValidatedInternship(
                "HR Recruitment Process",
                "Support in recruitment activities, candidate screening, interview coordination, and onboarding processes.",
                "Manpower Maroc", "Twin Center, Casablanca",
                LocalDate.now().minusDays(79), LocalDate.now().plusDays(13),
                emma, latifa, hr, 50, 30
        ));

        // PENDING_VALIDATION Internships (5)
        internships.add(createPendingInternship(
                "Mobile App Development",
                "Development of a cross-platform mobile application using React Native. Implementation of user authentication and real-time data synchronization.",
                "Jumia Morocco", "Marina Shopping Center, Casablanca",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(91),
                fatima, it, 10, 5
        ));

        internships.add(createPendingInternship(
                "Investment Banking Analysis",
                "Financial modeling, market research, and support in M&A transactions.",
                "CFG Bank", "Boulevard Moulay Slimane, Casablanca",
                LocalDate.now().plusDays(16), LocalDate.now().plusDays(107),
                alice, fin, 8, 3
        ));

        internships.add(createPendingInternship(
                "Pharmaceutical Research Assistant",
                "Laboratory work, clinical trial data collection, and research documentation.",
                "Sanofi Maroc", "Technopolis, Rabat",
                LocalDate.now().minusDays(9), LocalDate.now().plusDays(83),
                bob, health, 12, 4
        ));

        internships.add(createPendingInternship(
                "Corporate Legal Support",
                "Contract review, legal research, compliance documentation, and corporate governance support.",
                "Cabinet d'Avocats Bennani", "Avenue Hassan II, Rabat",
                LocalDate.now().plusDays(11), LocalDate.now().plusDays(102),
                claire, law, 6, 2
        ));

        internships.add(createPendingInternship(
                "Brand Management Internship",
                "Brand strategy development, market positioning analysis, and advertising campaign coordination.",
                "Maroc Telecom", "Avenue Annakhil, Rabat",
                LocalDate.now().minusDays(4), LocalDate.now().plusDays(88),
                david, mkt, 7, 1
        ));

        // REFUSED Internships (3)
        internships.add(createRefusedInternship(
                "Basic Data Entry",
                "Simple data entry tasks and file organization.",
                "Small Local Company", "Somewhere in Morocco",
                LocalDate.now().minusDays(61), LocalDate.now().minusDays(30),
                emma, hassan, it,
                "The internship does not meet the educational requirements. Tasks are too basic and do not align with IT program learning objectives. Please look for an internship with more technical responsibilities.",
                40, 30
        ));

        internships.add(createRefusedInternship(
                "Office Assistant Role",
                "General office support and administrative tasks.",
                "Generic Office LLC", "Downtown, Casablanca",
                LocalDate.now().minusDays(74), LocalDate.now().minusDays(43),
                fatima, sarah, fin,
                "Duration is too short (only 1 month). Minimum required duration is 3 months. Additionally, the role description lacks specific finance-related responsibilities.",
                45, 35
        ));

        internships.add(createRefusedInternship(
                "Unpaid Sales Position",
                "Sales and customer service with no compensation.",
                "Retail Store", "Local Market, Marrakech",
                LocalDate.now().minusDays(84), LocalDate.now().plusDays(7),
                alice, nadia, mkt,
                "This position does not comply with internship regulations. The role appears to be a regular sales position rather than a structured learning experience. Please seek opportunities with proper mentorship and educational objectives.",
                50, 40
        ));

        // DRAFT Internships (3)
        internships.add(createDraftInternship(
                "Cybersecurity Analysis",
                "Network security assessment, vulnerability testing, and security documentation.",
                "OCP Group", "Hay Erraha, Casablanca",
                LocalDate.now().plusDays(31), LocalDate.now().plusDays(122),
                bob, it, 5
        ));

        internships.add(createDraftInternship(
                "Employee Training Program Development",
                "Design and implementation of employee training modules and performance evaluation systems.",
                "ONCF", "Avenue Mohamed V, Rabat",
                LocalDate.now().plusDays(46), LocalDate.now().plusDays(138),
                claire, hr, 3
        ));

        internships.add(createDraftInternship(
                "Automotive Production Engineering",
                "",
                "PSA Peugeot Citroën", "Zone Industrielle, Kenitra",
                LocalDate.now().plusDays(62), LocalDate.now().plusDays(154),
                david, eng, 2
        ));

        // Save all internships
        for (Internship internship : internships) {
            if (internshipRepository.findByTitleAndCompanyName(
                    internship.getTitle(), internship.getCompanyName()).isEmpty()) {
                internshipRepository.save(internship);
            }
        }
        System.out.println("✓ Internships seeded (16 total: 5 validated, 5 pending, 3 refused, 3 drafts)");
    }

    private Internship createValidatedInternship(String title, String description,
            String company, String address, LocalDate startDate, LocalDate endDate,
            User student, User instructor, Sector sector, int createdDaysAgo, int validatedDaysAgo) {
        Internship internship = new Internship();
        internship.setTitle(title);
        internship.setDescription(description);
        internship.setCompanyName(company);
        internship.setCompanyAddress(address);
        internship.setStartDate(startDate);
        internship.setEndDate(endDate);
        internship.setStatus(InternshipStatus.VALIDATED);
        internship.setStudent(student);
        internship.setInstructor(instructor);
        internship.setSector(sector);
        internship.setCreatedAt(LocalDateTime.now().minusDays(createdDaysAgo));
        internship.setUpdatedAt(LocalDateTime.now().minusDays(validatedDaysAgo));
        internship.setSubmittedAt(LocalDateTime.now().minusDays(createdDaysAgo - 5));
        internship.setValidatedAt(LocalDateTime.now().minusDays(validatedDaysAgo));
        return internship;
    }

    private Internship createPendingInternship(String title, String description,
            String company, String address, LocalDate startDate, LocalDate endDate,
            User student, Sector sector, int createdDaysAgo, int submittedDaysAgo) {
        Internship internship = new Internship();
        internship.setTitle(title);
        internship.setDescription(description);
        internship.setCompanyName(company);
        internship.setCompanyAddress(address);
        internship.setStartDate(startDate);
        internship.setEndDate(endDate);
        internship.setStatus(InternshipStatus.PENDING_VALIDATION);
        internship.setStudent(student);
        internship.setSector(sector);
        internship.setCreatedAt(LocalDateTime.now().minusDays(createdDaysAgo));
        internship.setUpdatedAt(LocalDateTime.now().minusDays(submittedDaysAgo));
        internship.setSubmittedAt(LocalDateTime.now().minusDays(submittedDaysAgo));
        return internship;
    }

    private Internship createRefusedInternship(String title, String description,
            String company, String address, LocalDate startDate, LocalDate endDate,
            User student, User instructor, Sector sector, String refusalComment,
            int createdDaysAgo, int refusedDaysAgo) {
        Internship internship = new Internship();
        internship.setTitle(title);
        internship.setDescription(description);
        internship.setCompanyName(company);
        internship.setCompanyAddress(address);
        internship.setStartDate(startDate);
        internship.setEndDate(endDate);
        internship.setStatus(InternshipStatus.REFUSED);
        internship.setStudent(student);
        internship.setInstructor(instructor);
        internship.setSector(sector);
        internship.setRefusalComment(refusalComment);
        internship.setCreatedAt(LocalDateTime.now().minusDays(createdDaysAgo));
        internship.setUpdatedAt(LocalDateTime.now().minusDays(refusedDaysAgo));
        internship.setSubmittedAt(LocalDateTime.now().minusDays(createdDaysAgo - 5));
        return internship;
    }

    private Internship createDraftInternship(String title, String description,
            String company, String address, LocalDate startDate, LocalDate endDate,
            User student, Sector sector, int createdDaysAgo) {
        Internship internship = new Internship();
        internship.setTitle(title);
        internship.setDescription(description);
        internship.setCompanyName(company);
        internship.setCompanyAddress(address);
        internship.setStartDate(startDate);
        internship.setEndDate(endDate);
        internship.setStatus(InternshipStatus.DRAFT);
        internship.setStudent(student);
        internship.setSector(sector);
        internship.setCreatedAt(LocalDateTime.now().minusDays(createdDaysAgo));
        internship.setUpdatedAt(LocalDateTime.now().minusDays(1));
        return internship;
    }
}
