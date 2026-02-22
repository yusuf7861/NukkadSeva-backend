package com.nukkadseva.nukkadsevabackend.service;

import org.springframework.security.core.Authentication;
import com.nukkadseva.nukkadsevabackend.dto.response.CustomerDashboardDto;
import com.nukkadseva.nukkadsevabackend.dto.response.ProviderDashboardDto;

public interface DashboardService {
    CustomerDashboardDto getCustomerDashboard(Authentication authentication);

    ProviderDashboardDto getProviderDashboard(Authentication authentication);
}
