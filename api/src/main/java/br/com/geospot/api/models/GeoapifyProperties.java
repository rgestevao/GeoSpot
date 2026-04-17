package br.com.geospot.api.models;

public record GeoapifyProperties(
        String name,
        String city,
        String postcode,
        String street,
        String housenumber,
        String address_line1,
        String address_line2,
        String distance
) {
}
