package br.com.geospot.api.models;

import jakarta.validation.constraints.NotNull;

public record CreateUserResponse(
        @NotNull String name,
        @NotNull String email,
        @NotNull String accessToken,
        @NotNull String refreshToken
) {
}
