package br.com.geospot.api.models;

import java.util.List;

public record GetPlacesResponse(
        List<GeoapifyFeature> features
) {
}
