package br.com.geospot.api.services;

import br.com.geospot.api.db.UserRepository;
import br.com.geospot.api.db.UserStatusEnum;
import br.com.geospot.api.exceptions.ErrorCodeEnum;
import br.com.geospot.api.exceptions.FlowException;
import br.com.geospot.api.mappers.UserMapper;
import br.com.geospot.api.models.CreateUserRequest;
import br.com.geospot.api.models.CreateUserResponse;
import br.com.geospot.api.models.DeleteUserResponse;
import br.com.geospot.api.models.UpdatePasswordRequest;
import br.com.geospot.api.models.UpdatePasswordResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
        userMapped.setStatus(UserStatusEnum.ACTIVE);
        var user = userRepository.save(userMapped);
        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        return new CreateUserResponse(user.getName(), user.getEmail(), accessToken, refreshToken);
    }

    public DeleteUserResponse deleteUser(UUID userId) {
        var user = userRepository.findById(userId).orElseThrow(
                () -> new FlowException(ErrorCodeEnum.BAD_REQUEST, "User not found")
        );
        user.setUserId(userId);
        user.setStatus(UserStatusEnum.INACTIVE);
        var deletedUser = userRepository.save(user);
        return new DeleteUserResponse(deletedUser.getName(), deletedUser.getEmail(), deletedUser.getStatus());
    }

    public UpdatePasswordResponse updatePassword(UUID userId, UpdatePasswordRequest request) {
        var user = userRepository.findById(userId).orElseThrow(
                () -> new FlowException(ErrorCodeEnum.BAD_REQUEST, "User not found")
        );
        user.setUserId(userId);
        user.setPassword(passwordEncoder.encode(request.password()));
        var response = userRepository.save(user);
        return new UpdatePasswordResponse(response.getName(), response.getEmail());
    }
}
