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
}
