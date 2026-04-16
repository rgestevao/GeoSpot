package br.com.geospot.api.services;

import br.com.geospot.api.db.User;
import br.com.geospot.api.db.UserRepository;
import br.com.geospot.api.db.UserStatusEnum;
import br.com.geospot.api.exceptions.FlowException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        String secretKey = "12345678901234567890123456789012";
        jwtService = new JwtService(secretKey, userRepository);
        ReflectionTestUtils.setField(jwtService, "expiration", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L);
    }

    @Test
    void shouldGenerateToken() {
        User user = new User();
        user.setEmail("test@email.com");
        String token = jwtService.generateToken(user);
        Assertions.assertNotNull(token);
    }

    @Test
    void shouldValidateToken() {
        User user = new User();
        user.setEmail("test@email.com");
        String token = jwtService.generateToken(user);
        Assertions.assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void shouldExtractEmail() {
        User user = new User();
        user.setEmail("test@email.com");
        String token = jwtService.generateToken(user);
        String email = jwtService.extractEmail(token);
        Assertions.assertEquals("test@email.com", email);
    }

    @Test
    void shouldRefreshToken() {
        var user = new User("User Test", "test@email.com", "encodedPassword", UserStatusEnum.ACTIVE);
        String refreshToken = jwtService.generateRefreshToken(user);
        Mockito.when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        var response = jwtService.extractRefreshToken(refreshToken);
        Assertions.assertNotNull(response.accessToken());
        Assertions.assertEquals("test@email.com", response.email());
    }

    @Test
    void shouldThrowExceptionWhenTokenInvalid() {
        String invalidToken = "invalid_token";
        Assertions.assertThrows(FlowException.class, () -> {
            jwtService.extractRefreshToken(invalidToken);
        });
    }

    @Test
    void shouldThrowExceptionWhenTokenExpired() {
        var user = new User("User Test", "test@email.com", "encodedPassword", UserStatusEnum.ACTIVE);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", -1L);
        String token = jwtService.generateRefreshToken(user);
        Assertions.assertThrows(FlowException.class, () -> {
            jwtService.extractRefreshToken(token);
        });
    }

    @Test
    void shouldThrowExceptionWhenTokenMalformed() {
        String malformedToken = "abc.def.ghi";
        Assertions.assertThrows(FlowException.class, () -> {
            jwtService.extractRefreshToken(malformedToken);
        });
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        var user = new User("User Test", "test@email.com", "encodedPassword", UserStatusEnum.ACTIVE);
        String token = jwtService.generateRefreshToken(user);
        Mockito.when(userRepository.findByEmail("test@email.com"))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(FlowException.class, () -> {
            jwtService.extractRefreshToken(token);
        });
    }
}
