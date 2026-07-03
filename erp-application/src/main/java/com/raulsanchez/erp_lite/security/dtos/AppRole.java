package com.raulsanchez.erp_lite.security.dtos;

public record AppRole(String authority) {

    public AppRole {
        if (authority == null || authority.isBlank()) {
            throw new IllegalArgumentException("Authority cannot be blank");
        }
    }

    public static AppRole admin() {
        return new AppRole("ROLE_ADMIN");
    }

    public static AppRole manager() {
        return new AppRole("ROLE_MANAGER");
    }

    public static AppRole user() {
        return new AppRole("ROLE_USER");
    }
}
