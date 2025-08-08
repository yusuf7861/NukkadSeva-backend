package com.nukkadseva.nukkadsevabackend.repository;

import com.nukkadseva.nukkadsevabackend.entity.Users;
import com.nukkadseva.nukkadsevabackend.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByRole(Role role);

    boolean existsByEmail(String email);

    Optional<Users> findByEmail(String email);
}
