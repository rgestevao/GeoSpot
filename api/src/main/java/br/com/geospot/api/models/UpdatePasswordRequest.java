package br.com.geospot.api.models;

import jakarta.validation.constraints.NotNull;

public record UpdatePasswordRequest(
        @NotNull String password
) {
}
