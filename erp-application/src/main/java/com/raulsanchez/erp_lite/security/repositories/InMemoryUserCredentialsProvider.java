package com.raulsanchez.erp_lite.security.repositories;

import com.raulsanchez.erp_lite.security.dtos.AccountStatus;
import com.raulsanchez.erp_lite.security.dtos.AppRole;
import com.raulsanchez.erp_lite.security.dtos.AppUserDetails;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class InMemoryUserCredentialsProvider implements UserCredentialsProvider {

    private static final Map<String, AppUserDetails> USERS = Map.of(


            /*
             * ADMIN: $2a$12$DhoxF2VW7ihM56GwnscMjeBIeCh4ZKiE/bfagEdVhBXfXFxKXuyxS
             * MANAGER: $2a$12$rBU8qOyCKgq2Fr6.jcY7P.rxK8wxH3rx.KLF3UGW8eGzzosCS36Ny
             * EMPLOYEE: $2a$12$N8rhuMjCh4jR5a1az2mxQe5KCpHMgY6/oS7N2HX9wvkxwaohSJZEK
             */
            "admin", new AppUserDetails(
                    "admin",
                    // {bcrypt} — Spring Security 7 requiere password encoding
                    "$2a$12$DhoxF2VW7ihM56GwnscMjeBIeCh4ZKiE/bfagEdVhBXfXFxKXuyxS",
                    AccountStatus.ACTIVE,
                    Set.of(AppRole.admin())
            ),

            "manager", new AppUserDetails(
                    "manager",
                    "$2a$12$rBU8qOyCKgq2Fr6.jcY7P.rxK8wxH3rx.KLF3UGW8eGzzosCS36Ny",
                    AccountStatus.ACTIVE,
                    Set.of(AppRole.manager())
            ),

            "employee", new AppUserDetails(
                    "employee",
                    "$2a$12$N8rhuMjCh4jR5a1az2mxQe5KCpHMgY6/oS7N2HX9wvkxwaohSJZEK",
                    AccountStatus.ACTIVE,
                    Set.of(AppRole.user())
            )
    );

    @Override
    public Optional<AppUserDetails> findByUsername(String username) {
        return Optional.ofNullable(USERS.get(username));
    }
}
