package com.raulsanchez.erp_lite.persistence.rest.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "jsonplaceholder.api")
@Validated
public record JsonPlaceholderConfigModel(

        @NotBlank(message = "jsonplaceholder.api.base-url no puede estar vacío")
        String baseUrl,

        @NotBlank(message = "jsonplaceholder.api.users-endpoint no puede estar vacío")
        String usersEndpoint,

        @NotNull(message = "jsonplaceholder.api.connect-timeout es obligatorio")
        @Min(value = 1, message = "jsonplaceholder.api.connect-timeout debe ser mayor que 0")
        Integer connectTimeout,

        @NotNull(message = "jsonplaceholder.api.read-timeout es obligatorio")
        @Min(value = 1, message = "jsonplaceholder.api.read-timeout debe ser mayor que 0")
        Integer readTimeout,

        @NotNull(message = "jsonplaceholder.api.enabled es obligatorio")
        Boolean enabled

) {

    public String getUsersUrl() {
        return baseUrl + usersEndpoint;
    }
}