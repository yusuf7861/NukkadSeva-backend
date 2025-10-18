package com.nukkadseva.nukkadsevabackend.dto.request;

import com.nukkadseva.nukkadsevabackend.entity.enums.PaymentMethod;
import com.nukkadseva.nukkadsevabackend.entity.enums.Services;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {

    @NotNull(message = "Provider ID is required")
    private Long providerId;

    @NotNull(message = "Service Type is required")
    private Services serviceType;
    @NotNull(message = "Booking Date and Time is required")
    private LocalDateTime bookingDateTime;
    @NotNull(message = "Estimate Price is required")
    private BigDecimal priceEstimate;
    @NotNull(message = "Final Price is required")
    private BigDecimal finalPrice;
        @NotNull(message = "Payment Method is required")
    private PaymentMethod paymentMethod;
    private String note;
}
