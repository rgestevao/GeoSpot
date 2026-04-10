package br.com.geospot.api.services;

import br.com.geospot.api.db.User;
import br.com.geospot.api.db.UserRepository;
import br.com.geospot.api.exceptions.FlowException;
import br.com.geospot.api.models.LoginRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() {
        var email = "user@test.com";
        var password = "U$3rT3sT";
        var request = new LoginRequest(email, password);
        var user = new User(email, "encodedPassword");
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);
        Mockito.when(jwtService.generateToken(user)).thenReturn("fake-token");
        var result = authService.login(request);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.token()).isEqualTo("fake-token");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        var request = new LoginRequest("notfound@test.com", "123");
        Mockito.when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(FlowException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsInvalid() {
        var email = "user@test.com";
        var request = new LoginRequest(email, "wrong");
        var user = new User(email, "encoded");
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);
        Assertions.assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(FlowException.class)
                .hasMessage("Invalid credentials");
    }
}
