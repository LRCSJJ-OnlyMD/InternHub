package com.internhub.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final String frontendUrl;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromEmail,
            @Value("${app.frontend.url}") String frontendUrl) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.frontendUrl = frontendUrl;
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Email Verification");
        message.setText("Please click the link below to verify your email address:\n\n"
                + verificationUrl
                + "\n\nThis link will expire in 24 hours.");

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Reset Request");
        message.setText("You requested a password reset. Click the link below to reset your password:\n\n"
                + resetUrl
                + "\n\nThis link will expire in 24 hours.\n\n"
                + "If you didn't request this, please ignore this email.");

        mailSender.send(message);
    }

    public void sendTwoFactorEnabledEmail(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Two-Factor Authentication Enabled");
        message.setText("Two-factor authentication has been successfully enabled on your account.\n\n"
                + "You will now need to enter a verification code from your authenticator app when logging in.");

        mailSender.send(message);
    }

    public void send2FACodeEmail(String toEmail, int code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Your 2FA Verification Code");
        message.setText("Your two-factor authentication code is: " + code + "\n\n"
                + "This code will expire in 5 minutes.\n\n"
                + "If you didn't request this code, please secure your account immediately.");

        mailSender.send(message);
    }

    public void sendInstructorCredentials(String toEmail, String temporaryPassword, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Instructor Account Created - Internship Management System");
        message.setText("Dear " + firstName + ",\n\n"
                + "Your instructor account has been created by the administrator.\n\n"
                + "Login URL: " + frontendUrl + "/login\n"
                + "Email: " + toEmail + "\n"
                + "Temporary Password: " + temporaryPassword + "\n\n"
                + "IMPORTANT: Please log in and change your password immediately for security reasons.\n\n"
                + "If you have any questions, please contact the system administrator.\n\n"
                + "Best regards,\n"
                + "Internship Management System");

        mailSender.send(message);
    }

    // Instructor activation email
    public void sendInstructorActivationEmail(String toEmail, String activationToken, String firstName) {
        String activationUrl = frontendUrl + "/activate-account?token=" + activationToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Activate Your Instructor Account - InternHub");
        message.setText("Dear " + firstName + ",\n\n"
                + "Welcome to InternHub! Your instructor account has been created by the administrator.\n\n"
                + "To activate your account and set your password, please click the link below:\n\n"
                + activationUrl + "\n\n"
                + "This activation link will expire in 24 hours for security reasons.\n\n"
                + "Once activated, you'll be able to access the instructor portal and manage internship validations.\n\n"
                + "If you didn't expect this email or have any questions, please contact the system administrator.\n\n"
                + "Best regards,\n"
                + "InternHub Team");

        mailSender.send(message);
    }

    // Internship notification emails
    public void sendInternshipSubmittedEmail(String toEmail, String studentName, String internshipTitle) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("New Internship Submitted for Validation");
        message.setText("Dear Instructor,\n\n"
                + "A new internship has been submitted for validation:\n\n"
                + "Student: " + studentName + "\n"
                + "Internship Title: " + internshipTitle + "\n\n"
                + "Please log in to review and validate or refuse this internship.\n\n"
                + "Login URL: " + frontendUrl + "/login\n\n"
                + "Best regards,\n"
                + "Internship Management System");

        mailSender.send(message);
    }

    public void sendInternshipValidatedEmail(String toEmail, String studentName, String internshipTitle, String instructorName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Internship Validated - Congratulations!");
        message.setText("Dear " + studentName + ",\n\n"
                + "Great news! Your internship has been validated.\n\n"
                + "Internship Title: " + internshipTitle + "\n"
                + "Validated by: " + instructorName + "\n\n"
                + "You can now upload your internship report when ready.\n\n"
                + "Login URL: " + frontendUrl + "/login\n\n"
                + "Best regards,\n"
                + "Internship Management System");

        mailSender.send(message);
    }

    public void sendInternshipRefusedEmail(String toEmail, String studentName, String internshipTitle, String refusalComment) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Internship Refused - Action Required");
        message.setText("Dear " + studentName + ",\n\n"
                + "Your internship submission has been refused.\n\n"
                + "Internship Title: " + internshipTitle + "\n\n"
                + "Reason for refusal:\n"
                + refusalComment + "\n\n"
                + "Please review the feedback and make necessary corrections before resubmitting.\n\n"
                + "Login URL: " + frontendUrl + "/login\n\n"
                + "Best regards,\n"
                + "Internship Management System");

        mailSender.send(message);
    }

    public void sendReportUploadedEmail(String toEmail, String instructorName, String studentName, String internshipTitle) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Internship Report Uploaded");
        message.setText("Dear " + instructorName + ",\n\n"
                + "A student has uploaded their internship report:\n\n"
                + "Student: " + studentName + "\n"
                + "Internship Title: " + internshipTitle + "\n\n"
                + "You can now download and review the report.\n\n"
                + "Login URL: " + frontendUrl + "/login\n\n"
                + "Best regards,\n"
                + "Internship Management System");

        mailSender.send(message);
    }

    public void sendInstructorReassignedEmail(String toEmail, String studentName, String internshipTitle, String newInstructorName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Instructor Assignment Changed");
        message.setText("Dear " + studentName + ",\n\n"
                + "Your internship instructor has been reassigned by the administrator.\n\n"
                + "Internship Title: " + internshipTitle + "\n"
                + "New Instructor: " + newInstructorName + "\n\n"
                + "Login URL: " + frontendUrl + "/login\n\n"
                + "Best regards,\n"
                + "Internship Management System");

        mailSender.send(message);
    }

    // Generic email sending method for custom messages
    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
