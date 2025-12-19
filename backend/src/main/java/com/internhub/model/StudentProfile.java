package com.internhub.model;

import jakarta.persistence.*;

/**
 * Entity representing extended profile information for students. Linked to User
 * entity with one-to-one relationship. Follows SRP - manages student-specific
 * data only.
 */
@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "student_number", unique = true, length = 50)
    private String studentNumber;

    @Column(length = 100)
    private String major;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // Constructors
    public StudentProfile() {
    }

    public StudentProfile(User user, String studentNumber, String major, String academicYear) {
        this.user = user;
        this.studentNumber = studentNumber;
        this.major = major;
        this.academicYear = academicYear;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
