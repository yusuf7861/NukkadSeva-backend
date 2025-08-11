package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.ProviderDto;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.entity.Users;
import com.nukkadseva.nukkadsevabackend.entity.enums.Role;
import com.nukkadseva.nukkadsevabackend.repository.ProviderRepository;
import com.nukkadseva.nukkadsevabackend.repository.UserRepository;
import com.nukkadseva.nukkadsevabackend.services.AzureBlobStorageService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProviderService {

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AzureBlobStorageService azureBlobStorageService;

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_-+=<>?";
    private static final String PASSWORD_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL_CHARS;
    private static final SecureRandom random = new SecureRandom();

    public Provider registerProvider(ProviderDto providerDto) throws IOException {
        // Check for duplicate email
        if (providerRepository.findByEmail(providerDto.getEmail()).isPresent()) {
            throw new RuntimeException("A provider with this email already exists");
        }

        // Check for duplicate mobile number
        if (providerRepository.findByMobileNumber(providerDto.getMobileNumber()).isPresent()) {
            throw new RuntimeException("A provider with this mobile number already exists");
        }

        Provider provider = new Provider();
        provider.setFullName(providerDto.getFullName());
        provider.setDob(providerDto.getDob());
        provider.setMobileNumber(providerDto.getMobileNumber());
        provider.setEmail(providerDto.getEmail());
        provider.setBusinessName(providerDto.getBusinessName());
        provider.setServiceCategory(providerDto.getServiceCategory());
        provider.setServiceArea(providerDto.getServiceArea());
        provider.setExperience(providerDto.getExperience());
        provider.setLanguages(providerDto.getLanguages());
        provider.setFullAddress(providerDto.getFullAddress());
        provider.setState(providerDto.getState());
        provider.setCity(providerDto.getCity());
        provider.setPincode(providerDto.getPincode());
        provider.setGstin(providerDto.getGstin());
        provider.setBio(providerDto.getBio());
        provider.setAvailability(providerDto.getAvailability());
        provider.setAgreeToS(providerDto.isAgreeToS());
        provider.setAgreeToBgCheck(providerDto.isAgreeToBgCheck());

        // Upload images to Azure and set URLs
        if (providerDto.getPhotograph() != null && !providerDto.getPhotograph().isEmpty()) {
            String photographUrl = azureBlobStorageService.uploadFile(providerDto.getPhotograph(), "photograph");
            provider.setPhotograph(photographUrl);
        }

        if (providerDto.getGovtId() != null && !providerDto.getGovtId().isEmpty()) {
            String govtIdUrl = azureBlobStorageService.uploadFile(providerDto.getGovtId(), "govtId");
            provider.setGovtId(govtIdUrl);
        }

        if (providerDto.getQualification() != null && !providerDto.getQualification().isEmpty()) {
            String qualificationUrl = azureBlobStorageService.uploadFile(providerDto.getQualification(), "qualification");
            provider.setQualification(qualificationUrl);
        }

        if (providerDto.getPoliceVerification() != null && !providerDto.getPoliceVerification().isEmpty()) {
            String policeVerificationUrl = azureBlobStorageService.uploadFile(providerDto.getPoliceVerification(), "policeVerification");
            provider.setPoliceVerification(policeVerificationUrl);
        }

        if (providerDto.getProfilePicture() != null && !providerDto.getProfilePicture().isEmpty()) {
            String profilePictureUrl = azureBlobStorageService.uploadFile(providerDto.getProfilePicture(), "profilePicture");
            provider.setProfilePicture(profilePictureUrl);
        }

        // Set initial status and verification
        provider.setStatus("PENDING");
        provider.setIsEmailVerified(false);
        provider.setIsApproved(false);

        // Generate verification token
        String verificationToken = generateSecureToken();
        provider.setVerificationToken(verificationToken);
        provider.setTokenExpiresAt(LocalDateTime.now().plusHours(24)); // Token expires in 24 hours

        Provider savedProvider = providerRepository.save(provider);

        // Send verification email
        sendVerificationEmail(provider.getEmail(), verificationToken, provider.getId());

        return savedProvider;
    }

    public boolean verifyProviderEmail(String token) {
        Optional<Provider> providerOpt = providerRepository.findByVerificationToken(token);

        if (providerOpt.isEmpty()) {
            return false;
        }

        Provider provider = providerOpt.get();

        // Check if token is expired
        if (provider.getTokenExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Update provider status
        provider.setStatus("VERIFIED");
        provider.setIsEmailVerified(true);
        provider.setVerificationToken(null); // Clear the token
        provider.setTokenExpiresAt(null);

        providerRepository.save(provider);

        notifyAdminsOfNewVerifiedProvider(provider);

        return true;
    }

    public List<Provider> getPendingProviders() {
        return providerRepository.findByStatus("PENDING");
    }

    public List<Provider> getAllProviders() {
        return providerRepository.findAll();
    }

    public List<Provider> getProvidersByStatus(String status) {
        return providerRepository.findByStatus(status);
    }

    public Provider approveProvider(Long providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + providerId));
        provider.setStatus("APPROVED");

        // Generate a secure random password
        String generatedPassword = generateSecurePassword(12);

        // Create a corresponding User for the approved provider
        Users user = new Users();
        user.setEmail(provider.getEmail());
        user.setPassword(passwordEncoder.encode(generatedPassword));
        user.setRole(Role.SERVICE_PROVIDER);
        user.setVerified(true);
        userRepository.save(user);

        provider.setUser(user);
        Provider savedProvider = providerRepository.save(provider);

        // Send email with login credentials
        sendProviderApprovalEmail(provider.getEmail(), generatedPassword);

        return savedProvider;
    }

    public Provider rejectProvider(Long providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + providerId));
        provider.setStatus("REJECTED");

        sendProviderRejectionEmail(provider.getEmail());

        return providerRepository.save(provider);
    }

    public Optional<Provider> getProviderById(Long id) {
        return providerRepository.findById(id);
    }

    /**
     * Generates a secure random password with specified length
     */
    private String generateSecurePassword(int length) {
        StringBuilder password = new StringBuilder(length);

        // Ensure at least one of each character type
        password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        password.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
        password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));

        // Fill the rest of the password with random characters
        for (int i = 4; i < length; i++) {
            password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }

        // Shuffle the password characters
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = passwordArray[index];
            passwordArray[index] = passwordArray[i];
            passwordArray[i] = temp;
        }

        return new String(passwordArray);
    }

    /**
     * Sends an email to the provider with their login credentials
     */
    private void sendProviderApprovalEmail(String email, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("NukkadSeva Provider Account Approved");

            String emailContent =
                "<h2>Welcome to NukkadSeva!</h2>" +
                "<p>Your provider account has been approved. You can now login using the credentials below:</p>" +
                "<p><strong>Email:</strong> " + email + "</p>" +
                "<p><strong>Password:</strong> " + password + "</p>" +
                "<p>Please change your password after your first login.</p>" +
                "<p>Thank you for joining NukkadSeva!</p>";

            helper.setText(emailContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            System.err.println("Failed to send approval email: " + e.getMessage());
        }
    }

    /**
     * Sends a rejection notification email to the provider
     */
    private void sendProviderRejectionEmail(String email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("NukkadSeva Provider Application Status");

            String emailContent =
                "<h2>NukkadSeva Provider Application</h2>" +
                "<p>We regret to inform you that your application to become a service provider on NukkadSeva has not been approved at this time.</p>" +
                "<p>If you believe there has been an error or would like more information, please contact our support team.</p>" +
                "<p>Thank you for your interest in NukkadSeva.</p>";

            helper.setText(emailContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            System.err.println("Failed to send rejection email: " + e.getMessage());
        }
    }

    /**
     * Sends a verification email to the provider
     */
    private void sendVerificationEmail(String email, String token, Long providerId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("NukkadSeva Provider Email Verification");

            // Use localhost for local development, can be configured for production
            String baseUrl = "http://localhost:9002"; // Frontend URL for local development
            String emailContent = getString(token, providerId, baseUrl);

            helper.setText(emailContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
        }
    }

    @NotNull
    private static String getString(String token, Long providerId, String baseUrl) {
        String verificationLink = baseUrl + "/verify-email?token=" + token + "&id=" + providerId;

        String emailContent =
            "<h2>NukkadSeva Email Verification</h2>" +
            "<p>Thank you for registering as a service provider on NukkadSeva.</p>" +
            "<p>Please verify your email by clicking the link below:</p>" +
            "<p><a href=\"" + verificationLink + "\" style=\"background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block;\">Verify Email</a></p>" +
            "<p>Or copy and paste this link in your browser:</p>" +
            "<p>" + verificationLink + "</p>" +
            "<p>This link will expire in 24 hours.</p>" +
            "<p>If you did not register, please ignore this email.</p>";
        return emailContent;
    }

    /**
     * Notifies admins of a new verified provider (optional implementation)
     */
    private void notifyAdminsOfNewVerifiedProvider(Provider provider) {
        // Implement admin notification logic here (e.g., send email to admin)
    }

    /**
     * Generates a secure random token for verification
     */
    private String generateSecureToken() {
        return java.util.UUID.randomUUID().toString();
    }
}
