package com.nukkadseva.nukkadsevabackend.mapper;

import com.nukkadseva.nukkadsevabackend.dto.request.BookingRequest;
import com.nukkadseva.nukkadsevabackend.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;


@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookingMapper {
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "customer", ignore = true),
            @Mapping(target = "provider", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "cancelledAt", ignore = true),
            @Mapping(target = "completedAt", ignore = true),
            @Mapping(target = "paymentStatus", ignore = true),
            @Mapping(target = "providerNote", ignore = true),
            @Mapping(target = "review", ignore = true)
    })
    Booking toEntity(BookingRequest bookingRequest);

    BookingRequest toDto(Booking booking);
}