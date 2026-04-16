package br.com.geospot.api.models;

public record UpdateUserRequest(
        String name,
        String email,
        String password
) {
}
