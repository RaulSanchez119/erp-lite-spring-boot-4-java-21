package com.raulsanchez.erp_lite.controllers.auth;

import com.raulsanchez.erp_lite.dtos.AuthRequest;
import com.raulsanchez.erp_lite.dtos.AuthResponse;
import com.raulsanchez.erp_lite.security.dtos.AppUserDetails;
import com.raulsanchez.erp_lite.security.services.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        final var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.username(), authRequest.password()
                )
        );

        final var userDetails = (AppUserDetails) authentication.getPrincipal();
        assert userDetails != null;
        final var token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token));
    }
}