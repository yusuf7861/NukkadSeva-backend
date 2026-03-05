package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.service.EmailService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final Configuration freemarkerConfiguration;

    @Override
    public void sendForgotPasswordOtpEmail(String to, String name, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("name", name);
            templateModel.put("otp", otp);

            Template template = freemarkerConfiguration.getTemplate("forgot-password-otp.html");
            String htmlBody = FreeMarkerTemplateUtils.processTemplateIntoString(template, templateModel);

            helper.setTo(to);
            helper.setSubject("Password Reset Request - NukkadSeva");
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Forgot password OTP email sent successfully to {}", to);

        } catch (Exception e) {
            log.error("Failed to send forgot password email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendProviderApprovalEmail(String to, String password) {
        try {
            Map<String, Object> model = new HashMap<>();
            model.put("password", password);
            model.put("email", to);
            Template template = freemarkerConfiguration.getTemplate("provider-approval.html");
            String htmlBody = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            sendHtmlEmail(to, "NukkadSeva Provider Account Approved", htmlBody);
            log.info("Provider approval email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send approval email to {}", to, e);
        }
    }

    @Override
    public void sendProviderRejectionEmail(String to, String name, String reason) {
        try {
            Map<String, Object> model = new HashMap<>();
            model.put("name", name);
            model.put("email", to);
            model.put("reason", reason);
            Template template = freemarkerConfiguration.getTemplate("provider-rejection.html");
            String htmlBody = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            sendHtmlEmail(to, "NukkadSeva Provider Application Status", htmlBody);
            log.info("Provider rejection email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send rejection email to {}", to, e);
        }
    }

    @Override
    public void sendProviderVerificationEmail(String to, String token, Long providerId, String baseUrl) {
        try {
            String verificationLink = baseUrl + "/api/provider/verify-email?token=" + token + "&id=" + providerId;
            String htmlBody = "<h2>NukkadSeva Email Verification</h2>" +
                    "<p>Thank you for registering as a service provider on NukkadSeva.</p>" +
                    "<p>Please verify your email by clicking the link below:</p>" +
                    "<p><a href=\"" + verificationLink
                    + "\" style=\"background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block;\">Verify Email</a></p>"
                    + "<p>If you did not request this, please ignore this email.</p>";

            sendHtmlEmail(to, "NukkadSeva Provider Email Verification", htmlBody);
            log.info("Provider verification email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send provider verification email to {}", to, e);
        }
    }

    @Override
    public void sendCustomerVerificationEmail(String to, String token, String baseUrl) {
        try {
            Map<String, Object> model = new HashMap<>();
            model.put("email", to);
            model.put("verificationLink", baseUrl + "/api/verify-email?token=" + token);

            Template template = freemarkerConfiguration.getTemplate("user-email-verification.html");
            String htmlBody = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            sendHtmlEmail(to, "Verify your NukkadSeva account", htmlBody);
            log.info("Customer verification email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send customer verification email to {}", to, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }
}
