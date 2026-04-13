package br.com.geospot.api.models;

import jakarta.validation.constraints.NotNull;

public record UpdatePasswordResponse(
        @NotNull String name,
        @NotNull String email
) {
}
