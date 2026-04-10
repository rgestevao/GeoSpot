package br.com.geospot.api.models;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull
        String email,

        @NotNull
        String password
) {
}
