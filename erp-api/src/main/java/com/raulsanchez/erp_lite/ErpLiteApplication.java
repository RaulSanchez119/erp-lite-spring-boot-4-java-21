package com.raulsanchez.erp_lite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import javax.crypto.SecretKey;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class ErpLiteApplication implements CommandLineRunner {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final JwtEncoder jwtEncoder;
    private final SecretKey secretKey;

    public static void main(String[] args) {
        SpringApplication.run(ErpLiteApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        System.out.println("=== JwtEncoder ===");
        System.out.println("Bean: " + jwtEncoder.getClass().getSimpleName());

        System.out.println("=== SecretKey ===");
        System.out.println("Algoritmo: " + secretKey.getAlgorithm());
        System.out.println("Bytes: " + secretKey.getEncoded().length);
//        System.out.println("ADMIN: " + passwordEncoder.encode("admin"));
//        System.out.println("MANAGER: " + passwordEncoder.encode("manager"));
//        System.out.println("EMPLOYEE: " + passwordEncoder.encode("employee"));
    }
}