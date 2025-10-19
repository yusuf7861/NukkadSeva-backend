package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.request.BookingRequest;
import com.nukkadseva.nukkadsevabackend.entity.Booking;
import com.nukkadseva.nukkadsevabackend.entity.Customers;
import com.nukkadseva.nukkadsevabackend.entity.Provider;
import com.nukkadseva.nukkadsevabackend.entity.enums.BookingStatus;
import com.nukkadseva.nukkadsevabackend.entity.enums.PaymentStatus;
import com.nukkadseva.nukkadsevabackend.exception.BookingCreationException;
import com.nukkadseva.nukkadsevabackend.exception.CustomerNotFoundException;
import com.nukkadseva.nukkadsevabackend.exception.ProviderNotFoundException;
import com.nukkadseva.nukkadsevabackend.mapper.BookingMapper;
import com.nukkadseva.nukkadsevabackend.repository.BookingRepository;
import com.nukkadseva.nukkadsevabackend.repository.CustomerRepository;
import com.nukkadseva.nukkadsevabackend.repository.ProviderRepository;
import com.nukkadseva.nukkadsevabackend.service.BookingService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final CustomerRepository customerRepository;
    private final ProviderRepository providerRepository;
    private final BookingMapper bookingMapper;
        private final BookingRepository bookingRepository;


    @Override
    @Transactional
    public void createBooking(BookingRequest bookingRequest, Authentication authentication) {
        String email = authentication.getName();

        Customers customer = customerRepository.findByEmail(email).orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        Provider provider = providerRepository.findById(bookingRequest.getProviderId()).orElseThrow(() -> new ProviderNotFoundException("Provider not found"));
        if (provider == null) {
            log.error("Provider not found {}", bookingRequest.getProviderId());
        }

        Booking booking = bookingMapper.toEntity(bookingRequest);
        booking.setCustomer(customer);
        booking.setProvider(provider);

        if (bookingRequest.getBookingDateTime().isAfter(LocalDateTime.now().plusDays(7))) {
            throw new BookingCreationException("Booking cannot be scheduled more than 7 days in advance.");
        }
        booking.setBookingDateTime(bookingRequest.getBookingDateTime());
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.PENDING);

        try {
            bookingRepository.save(booking);
        } catch (CustomerNotFoundException | ProviderNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new BookingCreationException("Booking failed due to database error!", e);
        } catch (Exception e) {
            throw new BookingCreationException("An unexpected error occurred while creating the booking", e);
        }
    }
}
