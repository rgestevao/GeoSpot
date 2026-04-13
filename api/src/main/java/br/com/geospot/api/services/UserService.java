package br.com.geospot.api.services;

import br.com.geospot.api.db.UserRepository;
import br.com.geospot.api.exceptions.ErrorCodeEnum;
import br.com.geospot.api.exceptions.FlowException;
import br.com.geospot.api.mappers.UserMapper;
import br.com.geospot.api.models.CreateUserRequest;
import br.com.geospot.api.models.CreateUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    @Transactional
    public CreateUserResponse create(CreateUserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new FlowException(ErrorCodeEnum.BAD_REQUEST, "E-mail already exists");
        }
        var userMapped = userMapper.fromCreateUserRequest(request);
        userMapped.setPassword(passwordEncoder.encode(userMapped.getPassword()));
        var user = userRepository.save(userMapped);
        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        return new CreateUserResponse(user.getName(), user.getEmail(), accessToken, refreshToken);
    }
}
