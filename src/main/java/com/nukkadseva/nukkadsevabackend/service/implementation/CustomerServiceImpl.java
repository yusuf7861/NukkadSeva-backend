package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.request.CustomerAddressDto;
import com.nukkadseva.nukkadsevabackend.dto.request.CustomerProfileUpdateRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.CustomerRegistrationRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.AddressDto;
import com.nukkadseva.nukkadsevabackend.dto.response.CustomerProfileResponseDto;
import com.nukkadseva.nukkadsevabackend.entity.Address;
import com.nukkadseva.nukkadsevabackend.entity.CustomerAddress;
import com.nukkadseva.nukkadsevabackend.entity.Customers;
import com.nukkadseva.nukkadsevabackend.entity.Users;
import com.nukkadseva.nukkadsevabackend.entity.enums.BookingStatus;
import com.nukkadseva.nukkadsevabackend.entity.enums.Role;
import com.nukkadseva.nukkadsevabackend.exception.EmailAlreadyExistsException;
import com.nukkadseva.nukkadsevabackend.repository.*;
import com.nukkadseva.nukkadsevabackend.service.CustomerService;
import com.nukkadseva.nukkadsevabackend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    @Transactional
    public Customers updateCustomerProfile(CustomerProfileUpdateRequest request, String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        Customers customer = user.getCustomers();
        if (customer == null) {
            throw new UsernameNotFoundException("Customer profile not found for user: " + email);
        }

        if (request.getName() != null) {
            customer.setFullName(request.getName());
        }
        if (request.getPhone() != null) {
            customer.setMobileNumber(request.getPhone());
        }
        customer.setEmail(user.getEmail());

        if (request.getFullAddress() != null ||
                request.getCity() != null ||
                request.getState() != null ||
                request.getPincode() != null) {

            Address address = customer.getAddress();
            if (address == null) {
                address = new Address();
                customer.setAddress(address);
            }

            if (request.getFullAddress() != null) {
                address.setFullAddress(request.getFullAddress());
            }
            if (request.getCity() != null) {
                address.setCity(request.getCity());
            }
            if (request.getState() != null) {
                address.setState(request.getState());
            }
            if (request.getPincode() != null) {
                address.setPincode(request.getPincode());
            }
        }

        return customerRepository.save(customer);
    }

    public Customers getCustomerEntity(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileResponseDto getCustomerProfile(String email) {
        Customers customer = customerRepository.findWithAddressesByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with email: " + email));

        Long activeBookingsCount = bookingRepository.countByCustomerIdAndStatusIn(customer.getId(),
                List.of(BookingStatus.PENDING, BookingStatus.APPROVED));
        Long reviewsGivenCount = reviewRepository.countByCustomerId(customer.getId());

        List<com.nukkadseva.nukkadsevabackend.dto.response.CustomerAddressResponseDto> savedAddressesDto = null;
        if (customer.getSavedAddresses() != null) {
            savedAddressesDto = customer.getSavedAddresses().stream()
                    .map(addr -> com.nukkadseva.nukkadsevabackend.dto.response.CustomerAddressResponseDto.builder()
                            .id(addr.getId())
                            .type(addr.getType())
                            .flatName(addr.getFlatName())
                            .area(addr.getArea())
                            .landmark(addr.getLandmark())
                            .city(addr.getCity())
                            .state(addr.getState())
                            .pincode(addr.getPincode())
                            .isDefault(addr.isDefault())
                            .build())
                    .collect(Collectors.toList());
        }

        AddressDto addressDto = null;
        if (customer.getAddress() != null) {
            addressDto = AddressDto.builder()
                    .id(customer.getAddress().getId())
                    .fullAddress(customer.getAddress().getFullAddress())
                    .state(customer.getAddress().getState())
                    .city(customer.getAddress().getCity())
                    .pincode(customer.getAddress().getPincode())
                    .build();
        }

        return CustomerProfileResponseDto.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .mobileNumber(customer.getMobileNumber())
                .email(customer.getEmail())
                .photograph(customer.getPhotograph())
                .address(addressDto)
                .savedAddresses(savedAddressesDto)
                .activeBookingsCount(activeBookingsCount)
                .reviewsGivenCount(reviewsGivenCount)
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void customerRegistration(CustomerRegistrationRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        Users users = new Users();
        users.setEmail(request.getEmail());
        users.setPassword(passwordEncoder.encode(request.getPassword()));
        users.setRole(Role.CUSTOMER);
        users.setVerified(false);
        users.setVerificationToken(UUID.randomUUID().toString());
        users.setTokenExpiresAt(LocalDateTime.now().plusHours(24));

        // Create a Customers profile and set up associations
        Customers customer = new Customers();
        customer.setEmail(request.getEmail());

        // Add optional fields from registration
        if (request.getFullName() != null) {
            customer.setFullName(request.getFullName());
        }
        if (request.getMobileNumber() != null) {
            customer.setMobileNumber(request.getMobileNumber());
        }

        // Link both sides of association; Users owns the FK (customer_id)
        users.setCustomers(customer);
        customer.setUser(users);

        // Save owning side (Users); cascading will persist Customers as well
        userRepository.save(users);

        sendVerificationEmail(users.getEmail(), users.getVerificationToken());
    }

    private void sendVerificationEmail(String email, String token) {
        emailService.sendCustomerVerificationEmail(email, token, baseUrl);
    }

    // --- Address Management ---

    private CustomerAddressDto mapToDto(CustomerAddress address) {
        CustomerAddressDto dto = new CustomerAddressDto();
        dto.setId(address.getId());
        dto.setType(address.getType());
        dto.setFlatName(address.getFlatName());
        dto.setArea(address.getArea());
        dto.setLandmark(address.getLandmark());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPincode(address.getPincode());
        dto.setDefault(address.isDefault());
        return dto;
    }

    @Override
    @Transactional
    public CustomerAddressDto addAddress(String email, CustomerAddressDto addressDto) {
        Customers customer = getCustomerEntity(email);

        CustomerAddress address = new CustomerAddress();
        address.setCustomer(customer);
        address.setType(addressDto.getType());
        address.setFlatName(addressDto.getFlatName());
        address.setArea(addressDto.getArea());
        address.setLandmark(addressDto.getLandmark());
        address.setCity(addressDto.getCity());
        address.setState(addressDto.getState());
        address.setPincode(addressDto.getPincode());

        List<CustomerAddress> existingAddresses = customerAddressRepository.findByCustomer(customer);
        if (existingAddresses.isEmpty() || addressDto.isDefault()) {
            address.setDefault(true);
            if (addressDto.isDefault()) {
                existingAddresses.forEach(a -> {
                    a.setDefault(false);
                    customerAddressRepository.save(a);
                });
            }
        } else {
            address.setDefault(false);
        }

        return mapToDto(customerAddressRepository.save(address));
    }

    @Override
    @Transactional
    public CustomerAddressDto updateAddress(String email, Long addressId, CustomerAddressDto addressDto) {
        Customers customer = getCustomerEntity(email);
        CustomerAddress address = customerAddressRepository.findByIdAndCustomer(addressId, customer)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        address.setType(addressDto.getType());
        address.setFlatName(addressDto.getFlatName());
        address.setArea(addressDto.getArea());
        address.setLandmark(addressDto.getLandmark());
        address.setCity(addressDto.getCity());
        address.setState(addressDto.getState());
        address.setPincode(addressDto.getPincode());

        if (addressDto.isDefault() && !address.isDefault()) {
            setDefaultAddress(email, address.getId());
        }

        return mapToDto(customerAddressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(String email, Long addressId) {
        Customers customer = getCustomerEntity(email);
        CustomerAddress address = customerAddressRepository.findByIdAndCustomer(addressId, customer)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        boolean wasDefault = address.isDefault();
        customerAddressRepository.delete(address);

        if (wasDefault) {
            List<CustomerAddress> remaining = customerAddressRepository.findByCustomer(customer);
            if (!remaining.isEmpty()) {
                CustomerAddress newDefault = remaining.get(0);
                newDefault.setDefault(true);
                customerAddressRepository.save(newDefault);
            }
        }
    }

    @Override
    @Transactional
    public void setDefaultAddress(String email, Long addressId) {
        Customers customer = getCustomerEntity(email);

        List<CustomerAddress> allAddresses = customerAddressRepository.findByCustomer(customer);
        for (CustomerAddress addr : allAddresses) {
            if (addr.getId().equals(addressId)) {
                addr.setDefault(true);
            } else {
                addr.setDefault(false);
            }
            customerAddressRepository.save(addr);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAddressDto> getSavedAddresses(String email) {
        Customers customer = getCustomerEntity(email);
        return customerAddressRepository.findByCustomer(customer)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}
