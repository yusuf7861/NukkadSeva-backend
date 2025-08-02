package com.nukkadseva.nukkadsevabackend.mapper;

import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.UserResponse;
import com.nukkadseva.nukkadsevabackend.entity.Users;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UsersMapper {
    Users toEntity(UserRequest userRequest);

    UserRequest toDto(Users users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Users partialUpdate(UserRequest userRequest, @MappingTarget Users users);

    Users toEntity(UserResponse userResponse);

    UserResponse toDto1(Users users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Users partialUpdate(UserResponse userResponse, @MappingTarget Users users);
}