package br.com.geospot.api.models;

import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotNull String name,
        @NotNull String email,
        @NotNull String password
) {
}
