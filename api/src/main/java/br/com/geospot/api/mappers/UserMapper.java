package br.com.geospot.api.mappers;

import br.com.geospot.api.db.User;
import br.com.geospot.api.models.CreateUserRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @BeanMapping(ignoreByDefault = true)
    User fromCreateUserRequest(CreateUserRequest createUserRequest);
}
