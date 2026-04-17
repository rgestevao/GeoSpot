package br.com.geospot.api.models;

import jakarta.validation.constraints.NotNull;

public record GetPlacesRequest(
        @NotNull double lat,
        @NotNull double lon,
        @NotNull String category
) {
}
