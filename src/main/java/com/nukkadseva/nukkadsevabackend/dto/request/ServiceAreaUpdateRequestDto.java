package com.nukkadseva.nukkadsevabackend.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class ServiceAreaUpdateRequestDto {
    private List<String> pincodes;
}
