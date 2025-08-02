package com.nukkadseva.nukkadsevabackend.config;

import com.nukkadseva.nukkadsevabackend.entity.Users;
import com.nukkadseva.nukkadsevabackend.repository.UserRepository;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.IOException;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class RootAdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;
    private final Configuration freemarkerConfig;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) throws Exception {
        boolean isRootAdminExists = userRepository.existsByRole("ROOT_ADMIN");

        if (isRootAdminExists) {
            log.info("Root Admin found. Skipping the creating part of Root Admin.");
        } else {
            log.info("Root Admin not found. Creating Root Admin.");
            String password = generateRandomPassword(12);

            Users rootAdmin = new Users();
            rootAdmin.setEmail("yjamal12feb@gmail.com");
            rootAdmin.setPassword(passwordEncoder.encode(password));
            rootAdmin.setRole("ROOT_ADMIN");
            sendRootAdminCredentialsEmail(rootAdmin.getEmail(), rootAdmin.getEmail(), password);

            userRepository.save(rootAdmin);
            log.info("Root Admin created. Sending email to Root Admin.");
        }
    }

    private void sendRootAdminCredentialsEmail(String to, String email, String password) throws IOException, TemplateException, MessagingException {
        Map<String, Object> model = new HashMap<>();
        model.put("email", email);
        model.put("password", password);
        Template template = freemarkerConfig.getTemplate("root-admin-credentials.html");
        StringWriter stringWriter = new StringWriter();
        template.process(model, stringWriter);
        String htmlContent = stringWriter.toString();

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, true);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        helper.setTo(to);
        helper.setFrom("yjamal710@gmail.com");
        helper.setSubject("Your Root Admin Credentials - NukkadSeva");
        helper.setText(htmlContent, true);
        javaMailSender.send(message);
    }

    String generateRandomPassword(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for(int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
