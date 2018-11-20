package com.humblecode.humblecode.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Data
@Document
public class User implements UserDetails {

    static final String DEFAULT = "default";
    static final String MONTHLY = "monthly";
    static final String YEARLY = "yearly";
    
    @Id
    public UUID id = UUID.randomUUID();

    public String username;

    public String credentials;

    public String accountType = DEFAULT; //default/monthly/etc.

    public String loginType; // github/facebook/etc.

    public List<UUID> courseIdsPaidFor = new LinkedList<>();

    public List<TestResult> testResults = new LinkedList<>();

    public User() {}
    public User(String username, String credentials) {
        this.username = username;
        this.credentials = credentials;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.asList(new SimpleGrantedAuthority("user"));
    }

    @Override
    public String getPassword() {
        return credentials;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
