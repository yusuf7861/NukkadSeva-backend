package com.nukkadseva.nukkadsevabackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicProviderDto {
    private Long id;
    private String name;
    private String specialty;
    private Double rating;
    private Integer reviews;
    private String image;
    private String dataAiHint;
}
