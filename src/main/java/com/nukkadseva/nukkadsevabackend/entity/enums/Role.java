package com.nukkadseva.nukkadsevabackend.entity.enums;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;

public enum Role {
    CUSTOMER,
    ADMIN,
    ROOT_ADMIN,
    SERVICE_PROVIDER;

    public List<GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.name()));
    }

}
