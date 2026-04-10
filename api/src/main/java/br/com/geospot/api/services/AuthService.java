package br.com.geospot.api.services;

import br.com.geospot.api.db.UserRepository;
import br.com.geospot.api.exceptions.FlowException;
import br.com.geospot.api.models.LoginRequest;
import br.com.geospot.api.models.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        var userOptional = userRepository.findByEmail(request.email());
        if (userOptional.isEmpty()) {
            throw new FlowException("User not found");
        }
        var user = userOptional.get();
        var isMatched = passwordEncoder.matches(
                request.password(),
                user.getPassword()
        );
        if (!isMatched) {
            throw new FlowException("Invalid credentials");
        }
        var token = jwtService.generateToken(user);
        return new LoginResponse(user.getEmail(), token);
    }
}
