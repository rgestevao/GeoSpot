package br.com.geospot.api.services;

import br.com.geospot.api.client.GeoapifyClient;
import br.com.geospot.api.db.User;
import br.com.geospot.api.db.UserStatusEnum;
import br.com.geospot.api.exceptions.ErrorCodeEnum;
import br.com.geospot.api.exceptions.FlowException;
import br.com.geospot.api.models.GetPlacesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final GeoapifyClient client;

    public GetPlacesResponse searchPlaces(Authentication authentication, double lat, double lon, String category) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new FlowException(ErrorCodeEnum.UNAUTHORIZED, "User not authenticated");
        }
        var principal = authentication.getPrincipal();
        if (!(principal instanceof User user)) {
            throw new FlowException(ErrorCodeEnum.UNAUTHORIZED, "Invalid user");
        }
        if (UserStatusEnum.INACTIVE.equals(user.getStatus())) {
            throw new FlowException(ErrorCodeEnum.BAD_REQUEST, "Inactive user");
        }
        if (lat < -90 || lat > 90) {
            throw new FlowException(ErrorCodeEnum.BAD_REQUEST, "Invalid Latitude");
        }
        if (lon < -180 || lon > 180) {
            throw new FlowException(ErrorCodeEnum.BAD_REQUEST, "Invalid Longitude");
        }
        if (category == null || category.isBlank()) {
            throw new FlowException(ErrorCodeEnum.BAD_REQUEST, "Category is required");
        }
        return client.searchPlaces(lat, lon, category, 5000, 20);
    }
}
