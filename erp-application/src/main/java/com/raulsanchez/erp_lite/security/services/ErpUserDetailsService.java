package com.raulsanchez.erp_lite.security.services;

import com.raulsanchez.erp_lite.security.repositories.UserCredentialsProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Primary
public class ErpUserDetailsService implements UserDetailsService {

    private final UserCredentialsProvider inMemoryUserCredentialsProvider;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        return this.inMemoryUserCredentialsProvider.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}