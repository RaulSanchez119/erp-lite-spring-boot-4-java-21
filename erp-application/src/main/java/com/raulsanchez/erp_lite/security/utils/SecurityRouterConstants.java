package com.raulsanchez.erp_lite.security.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityRouterConstants {

    // Paths
    public static final String AUTH_PATH       = "/auth/**";
    public static final String COMMANDS_PATH   = "/api/commands/**";
    public static final String PRODUCTS_PATH   = "/api/queries/products/**";
    public static final String CATALOGS_PATH   = "/api/queries/catalogs/**";

    // Roles
    public static final String ROLE_ADMIN   = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_USER    = "USER";

    // JWT
    public static final String ROLES_CLAIM     = "roles";
    public static final String AUTHORITY_PREFIX = "";

    // OPEN API
    public static final String SWAGGER_PATH   = "/swagger-ui/**";
    public static final String API_DOCS_PATH  = "/v3/api-docs/**";
}
