package br.com.geospot.api.mappers;

import br.com.geospot.api.db.User;
import br.com.geospot.api.models.LoginRequest;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

    User toUser(LoginRequest loginRequest);
}
