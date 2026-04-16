package br.com.geospot.api.services;

import br.com.geospot.api.db.User;
import br.com.geospot.api.db.UserRepository;
import br.com.geospot.api.db.UserStatusEnum;
import br.com.geospot.api.exceptions.FlowException;
import br.com.geospot.api.mappers.UserMapper;
import br.com.geospot.api.models.CreateUserRequest;
import br.com.geospot.api.models.UpdatePasswordRequest;
import br.com.geospot.api.models.UpdateUserRequest;
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
    void shouldEncodePasswordBeforeSavingWhenRefreshToken() {
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

    @Test
    void shouldThrowExceptionWhenDeleteUserNotFound() {
        var userId = UUID.randomUUID();
        Assertions.assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(FlowException.class)
                .hasMessage("User not found");
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldUpdatePasswordSuccessfully() {
        var userId = UUID.randomUUID();
        var name = "User Test";
        var email = "user@test.com";
        var password = "U$3rT3sT";
        var request = new UpdatePasswordRequest(password);
        var user = new User(userId, name, email, "old-password", UserStatusEnum.ACTIVE);
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.encode(password)).thenReturn("new-encoded-password");
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
        var result = userService.updatePassword(userId, request);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.name()).isEqualTo(name);
        Assertions.assertThat(result.email()).isEqualTo(email);
        Assertions.assertThat(user.getPassword()).isEqualTo("new-encoded-password");
        Mockito.verify(passwordEncoder).encode(password);
        Mockito.verify(userRepository).save(Mockito.any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundWhenUpdatePassword() {
        var userId = UUID.randomUUID();
        var request = new UpdatePasswordRequest("123");
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> userService.updatePassword(userId, request))
                .isInstanceOf(FlowException.class)
                .hasMessage("User not found");
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldCallPasswordEncoder() {
        var userId = UUID.randomUUID();
        var request = new UpdatePasswordRequest("new-password");
        var user = new User(userId, "User", "user@test.com", "old", UserStatusEnum.ACTIVE);
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.encode(Mockito.any())).thenReturn("encoded");
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
        userService.updatePassword(userId, request);
        Mockito.verify(passwordEncoder).encode("new-password");
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        var userId = UUID.randomUUID();
        var name = "User Test";
        var email = "user@test.com";
        var password = "U$3rT3sT";
        var encryptedPassword = "encrypted-password";
        var request = new UpdateUserRequest(name, email, password);
        var user = new User(userId, name, email, encryptedPassword, UserStatusEnum.ACTIVE);
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.encode(Mockito.any())).thenReturn("encoded");
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
        var result = userService.update(userId, request);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.name()).isEqualTo(name);
        Assertions.assertThat(result.email()).isEqualTo(email);
        Mockito.verify(userRepository).save(Mockito.any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundInUpdateUser() {
        var userId = UUID.randomUUID();
        var request = new UpdateUserRequest("Name", "email@test.com", "123");
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> userService.update(userId, request))
                .isInstanceOf(FlowException.class)
                .hasMessageContaining("User not found");
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldThrowExceptionWhenUserIsInactive() {
        var userId = UUID.randomUUID();
        var user = new User(userId, "Name", "email@test.com", "pass", UserStatusEnum.INACTIVE);
        var request = new UpdateUserRequest("New Name", "new@email.com", "123");
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Assertions.assertThatThrownBy(() -> userService.update(userId, request))
                .isInstanceOf(FlowException.class)
                .hasMessageContaining("User is not active");
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldKeepOldValuesWhenRequestFieldsAreEmpty() {
        var userId = UUID.randomUUID();
        var user = new User(userId, "Old Name", "old@email.com", "encrypted", UserStatusEnum.ACTIVE);
        var request = new UpdateUserRequest("", "", "");
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.encode(Mockito.any())).thenReturn("encoded");
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
        var result = userService.update(userId, request);
        Assertions.assertThat(result.name()).isEqualTo("Old Name");
        Assertions.assertThat(result.email()).isEqualTo("old@email.com");
        Mockito.verify(passwordEncoder).encode("encrypted");
        Mockito.verify(userRepository).save(Mockito.any());
    }

    @Test
    void shouldEncodePasswordBeforeSavingWhenUpdateUser() {
        var userId = UUID.randomUUID();
        var user = new User(userId, "Name", "email@test.com", "old-pass", UserStatusEnum.ACTIVE);
        var request = new UpdateUserRequest("Name", "email@test.com", "new-pass");
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.encode("new-pass")).thenReturn("encoded-pass");
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
        userService.update(userId, request);
        Mockito.verify(passwordEncoder).encode("new-pass");
    }

    @Test
    void shouldUpdateOnlyNameWhenOtherFieldsAreEmpty() {
        var userId = UUID.randomUUID();
        var user = new User(userId, "Old Name", "old@email.com", "encrypted", UserStatusEnum.ACTIVE);
        var request = new UpdateUserRequest("New Name", "", "");
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.encode(Mockito.any())).thenReturn("encoded");
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
        var result = userService.update(userId, request);
        Assertions.assertThat(result.name()).isEqualTo("New Name");
        Assertions.assertThat(result.email()).isEqualTo("old@email.com");
    }

    @Test
    void shouldReencodePasswordEvenWhenEmptyRequestPassword() {
        var userId = UUID.randomUUID();
        var user = new User(userId, "Name", "email@test.com", "already-encrypted", UserStatusEnum.ACTIVE);
        var request = new UpdateUserRequest("Name", "email@test.com", "");
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.encode("already-encrypted")).thenReturn("re-encoded");
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
        userService.update(userId, request);
        Mockito.verify(passwordEncoder).encode("already-encrypted");
    }
}
