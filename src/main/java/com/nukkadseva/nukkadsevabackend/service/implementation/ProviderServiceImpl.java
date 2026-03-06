package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.request.ProviderDto;
import com.nukkadseva.nukkadsevabackend.dto.response.DashboardProviderDto;
import com.nukkadseva.nukkadsevabackend.dto.response.ProviderDetailDto;
import com.nukkadseva.nukkadsevabackend.dto.response.ProviderProfileResponseDto;
import com.nukkadseva.nukkadsevabackend.dto.response.ProviderSummaryDto;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.entity.Users;
import com.nukkadseva.nukkadsevabackend.entity.enums.ProviderStatus;
import com.nukkadseva.nukkadsevabackend.entity.enums.Role;
import com.nukkadseva.nukkadsevabackend.exception.ProviderNotFoundException;
import com.nukkadseva.nukkadsevabackend.mapper.ProviderMapper;
import com.nukkadseva.nukkadsevabackend.repository.ProviderRepository;
import com.nukkadseva.nukkadsevabackend.repository.UserRepository;
import com.nukkadseva.nukkadsevabackend.repository.CityRepository;
import com.nukkadseva.nukkadsevabackend.service.AzureBlobStorageService;
import com.nukkadseva.nukkadsevabackend.service.ProviderService;
import com.nukkadseva.nukkadsevabackend.service.EmailService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProviderServiceImpl implements ProviderService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProviderServiceImpl.class);

    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;
    private final AzureBlobStorageService azureBlobStorageService;
    private final ProviderMapper providerMapper;
    private final EmailService emailService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_-+=<>?";
    private static final String PASSWORD_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL_CHARS;
    private static final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public Provider registerProvider(ProviderDto providerDto) {
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
        provider.setServiceCategory(providerDto.getServiceCategory().toUpperCase());
        provider.setServiceArea(providerDto.getServiceArea());
        provider.setExperience(providerDto.getExperience());
        provider.setLanguages(providerDto.getLanguages());
        provider.setFullAddress(providerDto.getFullAddress());
        provider.setState(providerDto.getState());
        provider.setCity(providerDto.getCity().toUpperCase());
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
            String qualificationUrl = azureBlobStorageService.uploadFile(providerDto.getQualification(),
                    "qualification");
            provider.setQualification(qualificationUrl);
        }

        if (providerDto.getPoliceVerification() != null && !providerDto.getPoliceVerification().isEmpty()) {
            String policeVerificationUrl = azureBlobStorageService.uploadFile(providerDto.getPoliceVerification(),
                    "policeVerification");
            provider.setPoliceVerification(policeVerificationUrl);
        }

        if (providerDto.getProfilePicture() != null && !providerDto.getProfilePicture().isEmpty()) {
            String profilePictureUrl = azureBlobStorageService.uploadFile(providerDto.getProfilePicture(),
                    "profilePicture");
            provider.setProfilePicture(profilePictureUrl);
        }

        // Set initial status and verification
        provider.setStatus(ProviderStatus.PENDING);
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

    @Override
    public boolean verifyProviderEmail(String token) {
        Optional<Provider> providerOpt = providerRepository.findByVerificationToken(token);

        if (providerOpt.isEmpty()) {
            return false;
        }

        Provider provider = providerOpt.get();

        // Check if the token is expired
        if (provider.getTokenExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Update only email verification status, keep the status as PENDING
        provider.setIsEmailVerified(true);
        provider.setVerificationToken(null); // Clear the token
        provider.setTokenExpiresAt(null);

        providerRepository.save(provider);

        notifyAdminsOfNewVerifiedProvider(provider);

        return true;
    }

    @Override
    public List<Provider> getPendingProviders() {
        return providerRepository.findByStatus(ProviderStatus.PENDING);
    }

    @Override
    public List<Provider> getAllProviders() {
        return providerRepository.findAll();
    }

    @Override
    public List<Provider> getProvidersByStatus(ProviderStatus status) {
        return providerRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public Provider approveProvider(Long providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found with id: " + providerId));

        if (!ProviderStatus.PENDING.equals(provider.getStatus())) {
            throw new RuntimeException("Only pending providers can be approved");
        }

        if (!provider.getIsEmailVerified()) {
            throw new RuntimeException("Provider email must be verified before approval");
        }

        provider.setStatus(ProviderStatus.APPROVED);
        provider.setIsApproved(true);

        Optional<Users> existingUser = userRepository.findByEmail(provider.getEmail());
        Users user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setRole(Role.SERVICE_PROVIDER);
            user.setProvider(provider);
            provider.setUser(user);
            userRepository.save(user);
            Provider savedProvider = providerRepository.save(provider);
            sendProviderApprovalEmail(provider.getEmail(), "(Your existing account password)");
            return savedProvider;
        } else {
            String generatedPassword = generateSecurePassword();
            user = new Users();
            user.setEmail(provider.getEmail());
            user.setPassword(passwordEncoder.encode(generatedPassword));
            user.setRole(Role.SERVICE_PROVIDER);
            user.setVerified(true);
            user.setProvider(provider);
            provider.setUser(user);
            userRepository.save(user);
            Provider savedProvider = providerRepository.save(provider);
            sendProviderApprovalEmail(provider.getEmail(), generatedPassword);
            return savedProvider;
        }
    }

    @Override
    public Provider rejectProvider(Long providerId, String reason) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + providerId));

        if (!ProviderStatus.PENDING.equals(provider.getStatus())) {
            throw new RuntimeException("Only pending providers can be rejected");
        }

        provider.setStatus(ProviderStatus.REJECTED);
        provider.setRejectionReason(reason);

        sendProviderRejectionEmail(provider.getEmail(), reason);

        return providerRepository.save(provider);
    }

    @Override
    public Optional<Provider> getProviderById(Long id) {
        return providerRepository.findById(id);
    }

    /**
     * Generates a secure random password with a specified length
     */
    private String generateSecurePassword() {
        StringBuilder password = new StringBuilder(12);

        // Ensure at least one of each character type
        password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        password.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
        password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));

        // Fill the rest of the password with random characters
        for (int i = 4; i < 12; i++) {
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

    @Override
    public Page<DashboardProviderDto> searchProviders(String category, String city, String pincode, int page,
            int limit) {
        if (page < 1) {
            throw new IllegalArgumentException("Page number must be at least 1.");
        }
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100.");
        }
        Pageable pageable = PageRequest.of(page - 1, limit);

        Specification<Provider> spec = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.equal(root.get("status"), ProviderStatus.APPROVED));

            if (category != null && !category.isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("serviceCategory"), category));
            }
            if (city != null && !city.isEmpty()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("city"), city));
            }

            if (pincode != null && !pincode.isEmpty()) {
                // Join Provider with ProviderArea to check pincodes
                jakarta.persistence.criteria.Join<Provider, com.nukkadseva.nukkadsevabackend.entity.ProviderArea> areaJoin = root
                        .join("providerAreas", jakarta.persistence.criteria.JoinType.INNER);
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.isMember(pincode, areaJoin.get("pincodes")));
                query.distinct(true); // Since we are joining a collection, duplicates can appear if not distinct
            }
            return predicate;
        };

        Page<Provider> providerPage = providerRepository.findAll(spec, pageable);

        return providerPage.map(providerMapper::toDashboardProviderDto);
    }

    /**
     * Sends an email to the provider with their login credentials
     */
    @Override
    public void sendProviderApprovalEmail(String email, String password) {
        emailService.sendProviderApprovalEmail(email, password);
    }

    /**
     * Sends a rejection notification email to the provider
     */
    @Override
    public void sendProviderRejectionEmail(String email, String reason) {
        // Need to fetch name or just pass empty string. We'll pass empty for now.
        emailService.sendProviderRejectionEmail(email, "Provider", reason);
    }

    /**
     * Sends a verification email to the provider
     */
    @Override
    public void sendVerificationEmail(String email, String token, Long providerId) {
        emailService.sendProviderVerificationEmail(email, token, providerId, baseUrl);
    }

    /**
     * Notifies admins of a new verified provider (optional implementation)
     */
    @Override
    public void notifyAdminsOfNewVerifiedProvider(Provider provider) {
        // Implement admin notification logic here (e.g., send email to admin)
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderSummaryDto> getAllProvidersForAdmin() {
        log.info("Fetching all providers for admin dashboard");

        try {
            return providerRepository.findAll().stream().map(providerMapper::toProviderSummaryDto)
                    .collect(Collectors.toList());

        } catch (DataAccessException e) {
            log.error("Database error while fetching providers", e);
            throw new ServiceException("Database error occurred", e);
        } catch (Exception e) {
            log.error("Unexpected error while fetching providers", e);
            throw new ServiceException("Failed to retrieve providers", e);
        }
    }

    @Override
    public ProviderDetailDto getProviderByIdForAdmin(Long id) {
        log.info("Fetching provider details of ID: {}", id);

        try {
            Provider provider = providerRepository.findById(id)
                    .orElseThrow(() -> new ProviderNotFoundException("Provider not found:"));

            return providerMapper.toProviderDetailDto(provider);
        } catch (ProviderNotFoundException e) {
            log.warn("Provider not found for id {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching provider details", e);
            throw new ServiceException("Failed to retrieve provider details", e);
        }
    }

    /**
     * Generates a secure random token for verification
     */
    private String generateSecureToken() {
        return java.util.UUID.randomUUID().toString();
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderProfileResponseDto getProviderByEmail(String email) {
        Provider provider = providerRepository.findWithUserByEmail(email)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found with email: " + email));

        return com.nukkadseva.nukkadsevabackend.dto.response.ProviderProfileResponseDto.builder()
                .id(provider.getId())
                .fullName(provider.getFullName())
                .email(provider.getEmail())
                .mobileNumber(provider.getMobileNumber())
                .dob(provider.getDob())
                .profilePicture(provider.getProfilePicture())
                .bio(provider.getBio())
                .businessName(provider.getBusinessName())
                .serviceCategory(provider.getServiceCategory())
                .serviceArea(provider.getServiceArea())
                .experience(provider.getExperience())
                .languages(provider.getLanguages())
                .availability(provider.getAvailability())
                .fullAddress(provider.getFullAddress())
                .city(provider.getCity())
                .state(provider.getState())
                .pincode(provider.getPincode())
                .status(provider.getStatus().name())
                .isEmailVerified(provider.getIsEmailVerified())
                .isApproved(provider.getIsApproved())
                .rejectionReason(provider.getRejectionReason())
                .govtId(provider.getGovtId())
                .qualification(provider.getQualification())
                .policeVerification(provider.getPoliceVerification())
                .gstin(provider.getGstin())
                .build();
    }

    public Provider getProviderEntityByEmail(String email) {
        return providerRepository.findByEmail(email)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found with email: " + email));
    }

    @Override
    @Transactional
    public Provider updateServiceArea(String email, List<String> pincodes) {
        Provider provider = getProviderEntityByEmail(email);
        provider.setServiceArea(String.join(",", pincodes));
        return providerRepository.save(provider);
    }
}
