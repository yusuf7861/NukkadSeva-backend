package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.request.CustomerProfileUpdateRequest;
import com.nukkadseva.nukkadsevabackend.dto.request.UserRequest;
import com.nukkadseva.nukkadsevabackend.entity.Address;
import com.nukkadseva.nukkadsevabackend.entity.Customers;
import com.nukkadseva.nukkadsevabackend.entity.Users;
import com.nukkadseva.nukkadsevabackend.entity.enums.Role;
import com.nukkadseva.nukkadsevabackend.exception.EmailAlreadyExistsException;
import com.nukkadseva.nukkadsevabackend.repository.CustomerRepository;
import com.nukkadseva.nukkadsevabackend.repository.UserRepository;
import com.nukkadseva.nukkadsevabackend.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

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

    @Override
    public Customers getCustomerProfile(String email) {
        return customerRepository.findByEmail(email);
    }


    @Override
    @Transactional
    public void customerRegistration(UserRequest userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new EmailAlreadyExistsException(userRequest.getEmail());
        }

        Users users = new Users();
        users.setEmail(userRequest.getEmail());
        users.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        users.setRole(Role.CUSTOMER);

        // Create a Customers profile and set up associations
        Customers customer = new Customers();
        customer.setEmail(userRequest.getEmail());

        // Link both sides of association; Users owns the FK (customer_id)
        users.setCustomers(customer);
        customer.setUser(users);

        // Save owning side (Users); cascading will persist Customers as well
        userRepository.save(users);
    }
}
