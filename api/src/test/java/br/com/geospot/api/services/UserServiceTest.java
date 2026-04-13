package br.com.geospot.api.services;

import br.com.geospot.api.db.User;
import br.com.geospot.api.db.UserRepository;
import br.com.geospot.api.db.UserStatusEnum;
import br.com.geospot.api.exceptions.FlowException;
import br.com.geospot.api.mappers.UserMapper;
import br.com.geospot.api.models.CreateUserRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserSuccessfully() {
        var name = "User Test";
        var email = "user@test.com";
        var password = "U$3rT3sT";
        var request = new CreateUserRequest(name, email, password);
        var user = new User(name, email, "encoded-password", UserStatusEnum.ACTIVE);
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        Mockito.when(passwordEncoder.encode(ArgumentMatchers.anyString())).thenReturn("encoded-password");
        Mockito.when(userMapper.fromCreateUserRequest(request)).thenReturn(user);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
        Mockito.when(jwtService.generateToken(Mockito.any(User.class))).thenReturn("fake-token");
        Mockito.when(jwtService.generateRefreshToken(Mockito.any(User.class))).thenReturn("fake-refresh-token");
        var result = userService.create(request);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.name()).isEqualTo(name);
        Assertions.assertThat(result.email()).isEqualTo(email);
        Assertions.assertThat(result.accessToken()).isEqualTo("fake-token");
        Assertions.assertThat(result.refreshToken()).isEqualTo("fake-refresh-token");
        Mockito.verify(userRepository).findByEmail(email);
        Mockito.verify(userRepository).save(Mockito.any(User.class));
        Mockito.verify(jwtService).generateToken(Mockito.any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        var request = new CreateUserRequest("User", "user@test.com", "123");
        Mockito.when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User()));
        Assertions.assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(FlowException.class)
                .hasMessage("E-mail already exists");
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldEncodePasswordBeforeSaving() {
        var request = new CreateUserRequest("User", "user@test.com", "123");
        var user = new User("User", "user@test.com", "123", UserStatusEnum.ACTIVE);
        Mockito.when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        Mockito.when(userMapper.fromCreateUserRequest(Mockito.any())).thenReturn(user);
        Mockito.when(passwordEncoder.encode("123")).thenReturn("encoded-password");
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
        Mockito.when(jwtService.generateToken(Mockito.any())).thenReturn("token");
        Mockito.when(jwtService.generateRefreshToken(Mockito.any())).thenReturn("refresh");
        userService.create(request);
        Assertions.assertThat(user.getPassword()).isEqualTo("encoded-password");
    }

    @Test
    void shouldGenerateTokens() {
        var request = new CreateUserRequest("User", "user@test.com", "123");
        var user = new User("User", "user@test.com", "encoded", UserStatusEnum.ACTIVE);
        Mockito.when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        Mockito.when(userMapper.fromCreateUserRequest(Mockito.any())).thenReturn(user);
        Mockito.when(passwordEncoder.encode(Mockito.any())).thenReturn("encoded");
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
        Mockito.when(jwtService.generateToken(Mockito.any())).thenReturn("access");
        Mockito.when(jwtService.generateRefreshToken(Mockito.any())).thenReturn("refresh");
        var result = userService.create(request);
        Assertions.assertThat(result.accessToken()).isEqualTo("access");
        Assertions.assertThat(result.refreshToken()).isEqualTo("refresh");
    }

    @Test
    void shouldSoftDeleteUserSuccessfully() {
        var userId = UUID.randomUUID();
        var name = "User Test";
        var email = "user@test.com";
        var user = new User(name, email, "encoded-password", UserStatusEnum.ACTIVE);
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
        var result = userService.deleteUser(userId);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.status()).isEqualTo(UserStatusEnum.INACTIVE);
    }
}
