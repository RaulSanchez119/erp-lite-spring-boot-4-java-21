package com.raulsanchez.erp_lite.security.repositories;

import com.raulsanchez.erp_lite.security.dtos.AppUserDetails;

import java.util.Optional;

public interface UserCredentialsProvider {

    Optional<AppUserDetails> findByUsername(String username);
}