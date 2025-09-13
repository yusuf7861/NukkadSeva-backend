package com.nukkadseva.nukkadsevabackend.service.implementation;

import java.io.IOException;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.nukkadseva.nukkadsevabackend.dto.request.CustomerProfileUpdateRequest;
import com.nukkadseva.nukkadsevabackend.entity.Address;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.VerifyOtpRequest;
import com.nukkadseva.nukkadsevabackend.entity.Customers;
import com.nukkadseva.nukkadsevabackend.entity.Users;
import com.nukkadseva.nukkadsevabackend.entity.enums.Role;
import com.nukkadseva.nukkadsevabackend.exception.EmailAlreadyExistsException;
import com.nukkadseva.nukkadsevabackend.exception.InvalidOtpException;
import com.nukkadseva.nukkadsevabackend.exception.UserAuthenticationException;
import com.nukkadseva.nukkadsevabackend.jwt.JwtOtpUtil;
import com.nukkadseva.nukkadsevabackend.jwt.JwtUtil;
import com.nukkadseva.nukkadsevabackend.repository.CustomerRepository;
import com.nukkadseva.nukkadsevabackend.repository.UserRepository;
import com.nukkadseva.nukkadsevabackend.service.userservice.AppUserDetailsService;
import com.nukkadseva.nukkadsevabackend.service.userservice.UserService;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final JwtOtpUtil jwtOtpUtil;
    private final JavaMailSender javaMailSender;
    private final Configuration freemarkerConfig;

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
        return customerRepository.findByEmail(email);
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

        // Create and persist a Customers profile first to ensure ID is generated
        Customers customer = new Customers();
        customer.setEmail(userRequest.getEmail());
        Customers savedCustomer = customerRepository.save(customer);

        // Link both sides of association; Users owns the FK (customer_id)
        users.setCustomers(savedCustomer);
        savedCustomer.setUser(users);

        // Save owning side (Users) so the customer_id FK is persisted
        userRepository.save(users);
    }

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
        Template template = null;

        template = freemarkerConfig.getTemplate("user-verification-otp.html");

        StringWriter stringWriter = new StringWriter();

        template.process(model, stringWriter);

        String htmlContent = stringWriter.toString();

        MimeMessage message = javaMailSender.createMimeMessage();

        MimeMessageHelper helper = null;

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
}
