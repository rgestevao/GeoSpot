package br.com.geospot.api.client;

import br.com.geospot.api.exceptions.ErrorCodeEnum;
import br.com.geospot.api.exceptions.FlowException;
import br.com.geospot.api.models.GetPlacesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class GeoapifyClient {

    @Value("${geoapify.api-key}")
    private String geoapifyApiKey;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public GetPlacesResponse searchPlaces(
            double latitude,
            double longitude,
            String category,
            int radius,
            int limit
    ) {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://api.geoapify.com/v2/places")
                    .queryParam("categories", category)
                    .queryParam("filter", "circle:" + longitude + "," + latitude + "," + radius)
                    .queryParam("limit", limit)
                    .queryParam("apiKey", geoapifyApiKey)
                    .build()
                    .encode()
                    .toUri();
            var request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            var response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
            return mapper.readValue(response.body(), GetPlacesResponse.class);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            throw new FlowException(
                    ErrorCodeEnum.INTERNAL_SERVER_ERROR,
                    "Request interrupted"
            );
        } catch (java.io.IOException _) {
            throw new FlowException(
                    ErrorCodeEnum.INTERNAL_SERVER_ERROR,
                    "Error calling Geoapify API"
            );
        }
    }
}
