package br.com.geospot.api.mappers;

import br.com.geospot.api.db.User;
import br.com.geospot.api.models.CreateUserRequest;
import br.com.geospot.api.models.UpdateUserRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User fromCreateUserRequest(CreateUserRequest createUserRequest);
    User fromUpdateUserRequest(UpdateUserRequest createUserRequest);
}
