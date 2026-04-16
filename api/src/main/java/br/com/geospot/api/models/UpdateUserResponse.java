package br.com.geospot.api.models;

import jakarta.validation.constraints.NotNull;

public record UpdateUserResponse(
        @NotNull String name,
        @NotNull String email
) {
}
