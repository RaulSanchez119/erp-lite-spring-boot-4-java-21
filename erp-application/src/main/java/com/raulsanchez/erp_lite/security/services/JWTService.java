package com.raulsanchez.erp_lite.security.services;

import com.raulsanchez.erp_lite.security.dtos.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class JWTService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${app.security.expiration:86400}")
    private long expirationTime;

    public String generateToken(AppUserDetails userDetails) {
        Instant now = Instant.now();

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(expirationTime, ChronoUnit.SECONDS))
                .claims(map -> {
                    map.put("roles", roles);
                    map.put("enabled", userDetails.isEnabled());
                })
                .build();

        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(headers, claimsSet))
                .getTokenValue();

    }

    public boolean isTokenValid(String token) {
        try {
            Jwt decodedJwt = this.jwtDecoder.decode(token);

            return Instant.now().isBefore(Objects.requireNonNull(decodedJwt.getExpiresAt()));
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> extractRoles(String token) {
        return this.jwtDecoder
                .decode(token)
                .getClaim("roles");
    }
}
