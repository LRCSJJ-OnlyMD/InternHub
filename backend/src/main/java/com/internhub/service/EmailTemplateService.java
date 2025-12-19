package com.internhub.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service for rendering email templates using Thymeleaf. Provides methods to
 * generate HTML email content from templates.
 */
@Service
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    public EmailTemplateService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Render activation email template.
     *
     * @param userName User's name
     * @param activationLink Account activation link
     * @return Rendered HTML email content
     */
    public String renderActivationEmail(String userName, String activationLink) {
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("activationLink", activationLink);
        return templateEngine.process("email/activation-email", context);
    }

    /**
     * Render internship validated email template.
     *
     * @param params Map containing: studentName, instructorName,
     * internshipTitle, companyName, startDate, endDate, dashboardLink
     * @return Rendered HTML email content
     */
    public String renderInternshipValidatedEmail(Map<String, Object> params) {
        Context context = new Context();
        context.setVariables(params);
        return templateEngine.process("email/internship-validated", context);
    }

    /**
     * Render internship refused email template.
     *
     * @param params Map containing: studentName, instructorName,
     * internshipTitle, refusalComment, editLink
     * @return Rendered HTML email content
     */
    public String renderInternshipRefusedEmail(Map<String, Object> params) {
        Context context = new Context();
        context.setVariables(params);
        return templateEngine.process("email/internship-refused", context);
    }

    /**
     * Render password reset email template.
     *
     * @param userName User's name
     * @param resetLink Password reset link
     * @return Rendered HTML email content
     */
    public String renderPasswordResetEmail(String userName, String resetLink) {
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("resetLink", resetLink);
        return templateEngine.process("email/password-reset", context);
    }

    /**
     * Render generic notification email template.
     *
     * @param params Map containing notification details
     * @return Rendered HTML email content
     */
    public String renderGenericEmail(String templateName, Map<String, Object> params) {
        Context context = new Context();
        context.setVariables(params);
        return templateEngine.process("email/" + templateName, context);
    }
}
