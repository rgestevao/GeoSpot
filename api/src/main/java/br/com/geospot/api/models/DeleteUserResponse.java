package br.com.geospot.api.models;

import br.com.geospot.api.db.UserStatusEnum;
import jakarta.validation.constraints.NotNull;

public record DeleteUserResponse(
        @NotNull String name,
        @NotNull String email,
        @NotNull UserStatusEnum status
) {
}
