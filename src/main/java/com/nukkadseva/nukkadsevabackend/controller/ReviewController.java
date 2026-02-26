package com.nukkadseva.nukkadsevabackend.controller;

import com.nukkadseva.nukkadsevabackend.dto.request.ReviewRequestDto;
import com.nukkadseva.nukkadsevabackend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import com.nukkadseva.nukkadsevabackend.dto.response.ReviewResponseDto;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<ReviewResponseDto> submitReview(
            @Valid @RequestBody ReviewRequestDto requestDto,
            Authentication authentication) {

        ReviewResponseDto responseDto = reviewService.submitReview(requestDto, authentication);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/provider")
    public ResponseEntity<java.util.List<ReviewResponseDto>> getProviderReviews(
            Authentication authentication) {
        java.util.List<ReviewResponseDto> reviews = reviewService.getProviderReviews(authentication);
        return ResponseEntity.ok(reviews);
    }
}
