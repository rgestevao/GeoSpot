package br.com.geospot.api.services;

import br.com.geospot.api.db.User;
import br.com.geospot.api.db.UserRepository;
import br.com.geospot.api.mappers.UserMapper;
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
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() {
        var email = "user@test.com";
        var password = "U$3rT3sT";
        var request = new LoginRequest(email, "U$3rT3sT");
        var user = new User(email, password);
        Mockito.when(userMapper.toUser(request)).thenReturn(user);
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(password, "U$3rT3sT"))
                .thenReturn(true);
        var result = authService.login(request);
        Assertions.assertThat(result).isNotNull();
    }
}
