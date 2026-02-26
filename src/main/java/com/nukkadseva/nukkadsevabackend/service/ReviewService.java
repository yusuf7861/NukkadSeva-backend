package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.request.ReviewRequestDto;
import com.nukkadseva.nukkadsevabackend.dto.response.ReviewResponseDto;
import org.springframework.security.core.Authentication;
import java.util.List;

public interface ReviewService {
    ReviewResponseDto submitReview(ReviewRequestDto requestDto, Authentication authentication);

    List<ReviewResponseDto> getProviderReviews(Authentication authentication);
}
