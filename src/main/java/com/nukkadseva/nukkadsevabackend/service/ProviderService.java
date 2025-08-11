package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.ProviderDto;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.entity.Users;
import com.nukkadseva.nukkadsevabackend.entity.enums.Role;
import com.nukkadseva.nukkadsevabackend.repository.ProviderRepository;
import com.nukkadseva.nukkadsevabackend.repository.UserRepository;
import com.nukkadseva.nukkadsevabackend.services.AzureBlobStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.security.SecureRandom;
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

        // Set default values for approval and verification status
        provider.setIsApproved(false);
        provider.setIsEmailVerified(false);

        // createdAt is automatically set by @CreationTimestamp annotation

        return providerRepository.save(provider);
    }

    public List<Provider> getPendingProviders() {
        return providerRepository.findByStatus("PENDING");
    }

    public List<Provider> getAllProviders() {
        return providerRepository.findAll();
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
}
