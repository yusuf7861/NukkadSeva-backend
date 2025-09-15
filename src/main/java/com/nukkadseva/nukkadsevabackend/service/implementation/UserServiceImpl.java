package com.nukkadseva.nukkadsevabackend.service.implementation;

import java.io.IOException;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.nukkadseva.nukkadsevabackend.entity.Customers;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.repository.ProviderRepository;
import com.nukkadseva.nukkadsevabackend.service.AzureBlobStorageService;
import jakarta.transaction.Transactional;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.VerifyOtpRequest;
import com.nukkadseva.nukkadsevabackend.entity.Users;
import com.nukkadseva.nukkadsevabackend.exception.EmailAlreadyExistsException;
import com.nukkadseva.nukkadsevabackend.exception.InvalidOtpException;
import com.nukkadseva.nukkadsevabackend.exception.UserAuthenticationException;
import com.nukkadseva.nukkadsevabackend.security.JwtOtpUtil;
import com.nukkadseva.nukkadsevabackend.security.JwtUtil;
import com.nukkadseva.nukkadsevabackend.repository.CustomerRepository;
import com.nukkadseva.nukkadsevabackend.repository.UserRepository;
import com.nukkadseva.nukkadsevabackend.util.AppUserDetailsService;
import com.nukkadseva.nukkadsevabackend.service.UserService;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AuthenticationManager authenticationManager;
    private final ProviderRepository providerRepository;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final JwtOtpUtil jwtOtpUtil;
    private final JavaMailSender javaMailSender;
    private final Configuration freemarkerConfig;
    private final AzureBlobStorageService azureBlobStorageService;



    @Override
    public String login(UserRequest userRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userRequest.getEmail(), userRequest.getPassword()
                    )
            );
            final UserDetails userDetails = userDetailsService.loadUserByUsername(userRequest.getEmail());

            final String token = jwtUtil.generateToken(userDetails);
            return token;
        } catch (AuthenticationException e) {
            throw new UserAuthenticationException("Invalid email or password");
        }
    }

    @Override
    public String sendVerificationOtp(String email) throws MessagingException, IOException, TemplateException {
        Optional<Users> byEmail = userRepository.findByEmail(email);

        if (byEmail.isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        SecureRandom secureRandom = new SecureRandom();
        String resetOtp = String.format("%06d", secureRandom.nextInt(1_000_000));
        String token = jwtOtpUtil.generateOtpToken(email, resetOtp, 10);

        Map<String, Object> model = new HashMap<>();
        model.put("email", email);
        model.put("otp", resetOtp);
        Template template;

        template = freemarkerConfig.getTemplate("user-verification-otp.html");

        StringWriter stringWriter = new StringWriter();

        template.process(model, stringWriter);

        String htmlContent = stringWriter.toString();

        MimeMessage message = javaMailSender.createMimeMessage();

        MimeMessageHelper helper;

        helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setFrom("yjamal710@gmail.com");
        helper.setSubject("Account Verification OTP - NukkadSeva");
        helper.setText(htmlContent, true);
        javaMailSender.send(message);

        return token;
    }

    @Override
    public boolean verifyOtp(VerifyOtpRequest request) {
        try {
            Claims claims = jwtOtpUtil.validateTokenAndGetClaims(request.getToken());
            String otpFromToken = (String) claims.get("otp");

            if (!request.getOtp().equals(otpFromToken)) {
                throw new InvalidOtpException("OTP does not match");
            }

            return true;
        } catch (ExpiredJwtException | MalformedJwtException jwtException) {
            throw new InvalidOtpException("OTP token is invalid or expired.");
        }
    }

    @Transactional
    @Override
    public void updateProfilePicture(MultipartFile file, Authentication authentication) {
        String email = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No role assigned"));

        switch (role) {
            case "CUSTOMER":
                Customers customer = customerRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Customer not found"));

                String imageLink = azureBlobStorageService.uploadFile(file, "profilePicture");
                customer.setPhotograph(imageLink);
                customerRepository.save(customer);
                break;

            case "SERVICE_PROVIDER":
                Provider provider = providerRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Provider not found"));

                String providerImageLink = azureBlobStorageService.uploadFile(file, "profilePictures");
                provider.setPhotograph(providerImageLink);
                providerRepository.save(provider);
                break;

            default:
                throw new IllegalArgumentException("Unsupported role: " + role);
        }
    }
}
