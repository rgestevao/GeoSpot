package br.com.geospot.api.controllers;

import br.com.geospot.api.models.GetPlacesResponse;
import br.com.geospot.api.services.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
@Tag(name = "Place", description = "API Place Management")
public class PlaceController {

    private final PlaceService service;

    @GetMapping
    @Operation(summary = "Search places flow")
    public GetPlacesResponse searchPlaces(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam String category,
            Authentication authentication
    ) {
        return service.searchPlaces(authentication, lat, lon, category);
    }
}
