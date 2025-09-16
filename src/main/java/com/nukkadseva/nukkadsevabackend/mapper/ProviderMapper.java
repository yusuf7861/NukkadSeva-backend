package com.nukkadseva.nukkadsevabackend.mapper;

import com.azure.core.http.rest.Page;
import com.nukkadseva.nukkadsevabackend.dto.request.ProviderDto;
import com.nukkadseva.nukkadsevabackend.dto.response.DashboardProviderDto;
import com.nukkadseva.nukkadsevabackend.dto.response.ProviderDetailDto;
import com.nukkadseva.nukkadsevabackend.dto.response.ProviderSummaryDto;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProviderMapper {

    /**
     * Maps a Provider entity to a ProviderSummaryDto for the admin dashboard list.
     */
    ProviderSummaryDto toProviderSummaryDto(Provider provider);

    /**
     * Maps a list of Provider entities to a list of ProviderSummaryDtos.
     */
    List<ProviderSummaryDto> toProviderSummaryDtoList(List<Provider> providers);

    /**
     * Maps a Provider entity to a ProviderDetailDto for the detailed admin view.
     */
    ProviderDetailDto toProviderDetailDto(Provider provider);

    /**
     * Maps a ProviderDto (from the registration form) to a Provider entity.
     * Note: We ignore MultipartFile fields as they are handled separately in the service layer.
     */
    @Mapping(target = "photograph", ignore = true)
    @Mapping(target = "govtId", ignore = true)
    @Mapping(target = "qualification", ignore = true)
    @Mapping(target = "policeVerification", ignore = true)
    @Mapping(target = "profilePicture", ignore = true)
    Provider toProvider(ProviderDto providerDto);

    /**
     * Maps a Provider entity to a DashboardProviderDto for the public dashboard.
     */
    Page<DashboardProviderDto> toDashboardProviderDto(Provider provider);

    /**
     * Maps a list of Provider entities to a list of DashboardProviderDtos.
     */
    List<DashboardProviderDto> toDashboardProviderDtoList(List<Provider> providers);
}