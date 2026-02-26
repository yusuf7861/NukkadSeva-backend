package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.request.CustomerProfileUpdateRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.entity.Address;
import com.nukkadseva.nukkadsevabackend.entity.Customers;
import com.nukkadseva.nukkadsevabackend.entity.Users;
import com.nukkadseva.nukkadsevabackend.entity.enums.Role;
import com.nukkadseva.nukkadsevabackend.exception.EmailAlreadyExistsException;
import com.nukkadseva.nukkadsevabackend.repository.CustomerRepository;
import com.nukkadseva.nukkadsevabackend.repository.UserRepository;
import com.nukkadseva.nukkadsevabackend.service.CustomerService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final Configuration freemarkerConfig;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    @Transactional
    public Customers updateCustomerProfile(CustomerProfileUpdateRequest request, String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        Customers customer = user.getCustomers();
        if (customer == null) {
            throw new UsernameNotFoundException("Customer profile not found for user: " + email);
        }

        if (request.getName() != null) {
            customer.setFullName(request.getName());
        }
        if (request.getPhone() != null) {
            customer.setMobileNumber(request.getPhone());
        }
        customer.setEmail(user.getEmail());

        if (request.getFullAddress() != null ||
                request.getCity() != null ||
                request.getState() != null ||
                request.getPincode() != null) {

            Address address = customer.getAddress();
            if (address == null) {
                address = new Address();
                customer.setAddress(address);
            }

            if (request.getFullAddress() != null) {
                address.setFullAddress(request.getFullAddress());
            }
            if (request.getCity() != null) {
                address.setCity(request.getCity());
            }
            if (request.getState() != null) {
                address.setState(request.getState());
            }
            if (request.getPincode() != null) {
                address.setPincode(request.getPincode());
            }
        }

        return customerRepository.save(customer);
    }

    @Override
    public Customers getCustomerProfile(String email) {
        return customerRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Customer not found with email: " + email));
    }


    @Override
    @Transactional
    public void customerRegistration(UserRequest userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new EmailAlreadyExistsException(userRequest.getEmail());
        }

        Users users = new Users();
        users.setEmail(userRequest.getEmail());
        users.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        users.setRole(Role.CUSTOMER);
        users.setVerified(false);
        users.setVerificationToken(UUID.randomUUID().toString());
        users.setTokenExpiresAt(LocalDateTime.now().plusHours(24));

        // Create a Customers profile and set up associations
        Customers customer = new Customers();
        customer.setEmail(userRequest.getEmail());

        // Link both sides of association; Users owns the FK (customer_id)
        users.setCustomers(customer);
        customer.setUser(users);

        // Save owning side (Users); cascading will persist Customers as well
        userRepository.save(users);

        sendVerificationEmail(users.getEmail(), users.getVerificationToken());
    }

    private void sendVerificationEmail(String email, String token) {
        try {
            Map<String, Object> model = new HashMap<>();
            model.put("email", email);
            model.put("verificationLink", baseUrl + "/api/verify-email?token=" + token);

            Template template = freemarkerConfig.getTemplate("user-email-verification.html");
            StringWriter stringWriter = new StringWriter();
            template.process(model, stringWriter);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("Verify your NukkadSeva account");
            helper.setText(stringWriter.toString(), true);
            mailSender.send(message);
        } catch (MessagingException | IOException | TemplateException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}
