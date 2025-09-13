package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.ProviderDto;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import freemarker.template.TemplateException;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ProviderService {
    Provider registerProvider(ProviderDto providerDto) throws IOException;
    boolean verifyProviderEmail(String token);
    List<Provider> getPendingProviders();
    List<Provider> getAllProviders();
    List<Provider> getProvidersByStatus(String status);
    Provider approveProvider(Long providerId) throws TemplateException, IOException;
    Provider rejectProvider(Long providerId, String reason) throws TemplateException, IOException;
    Optional<Provider> getProviderById(Long id);
    Page<Provider> searchProviders(String category, String city, String pincode, int page, int limit);

    void sendProviderApprovalEmail(String email, String password) throws IOException, TemplateException;

    void sendProviderRejectionEmail(String email, String reason) throws IOException, TemplateException;

    void sendVerificationEmail(String email, String token, Long providerId);

    void notifyAdminsOfNewVerifiedProvider(Provider provider);
}
