package br.com.geospot.api.services;

import br.com.geospot.api.client.GeoapifyClient;
import br.com.geospot.api.db.User;
import br.com.geospot.api.exceptions.FlowException;
import br.com.geospot.api.models.GetPlacesResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @Mock
    private GeoapifyClient client;

    @InjectMocks
    private PlaceService service;

    @Mock
    private Authentication authentication;

    @Test
    void shouldReturnPlacesWhenValidInput() {
        var user = new User();
        user.setEmail("user@test.com");
        Mockito.when(authentication.getPrincipal()).thenReturn(user);
        var mockResponse = new GetPlacesResponse(List.of());
        Mockito.when(client.searchPlaces(-23.0, -46.0, "catering.restaurant", 5000, 20))
                .thenReturn(mockResponse);
        var response = service.searchPlaces(authentication, -23.0, -46.0, "catering.restaurant");
        Assertions.assertNotNull(response);
        Mockito.verify(client).searchPlaces(-23.0, -46.0, "catering.restaurant", 5000, 20);
    }

    @ParameterizedTest
    @MethodSource("invalidInputsProvider")
    void shouldThrowExceptionWhenInvalidInputs(
            double lat,
            double lon,
            String category
    ) {
        var user = new User();
        user.setEmail("user@test.com");
        Mockito.when(authentication.getPrincipal()).thenReturn(user);
        Assertions.assertThrows(FlowException.class, () ->
                service.searchPlaces(authentication, lat, lon, category)
        );
        Mockito.verifyNoInteractions(client);
    }

    private static Stream<Arguments> invalidInputsProvider() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(-100.0, -46.0, "catering.restaurant"),
                org.junit.jupiter.params.provider.Arguments.of(-23.0, -200.0, "catering.restaurant"),
                org.junit.jupiter.params.provider.Arguments.of(-23.0, -46.0, null)
        );
    }

    @Test
    void shouldAlwaysUseDefaultRadiusAndLimit() {
        var user = new User();
        user.setEmail("user@test.com");
        Mockito.when(authentication.getPrincipal()).thenReturn(user);
        Mockito.when(client.searchPlaces(
                ArgumentMatchers.anyDouble(),
                ArgumentMatchers.anyDouble(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt()
        )).thenReturn(new GetPlacesResponse(List.of()));
        service.searchPlaces(authentication, -23.0, -46.0, "catering.restaurant");
        Mockito.verify(client).searchPlaces(-23.0, -46.0, "catering.restaurant", 5000, 20);
    }

    @Test
    void shouldThrowWhenAuthenticationIsNull() {
        Assertions.assertThrows(FlowException.class, () ->
                service.searchPlaces(null, -23.0, -46.0, "catering.restaurant")
        );
    }
}
