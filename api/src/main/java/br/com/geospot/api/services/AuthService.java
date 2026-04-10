package br.com.geospot.api.services;

import br.com.geospot.api.db.UserRepository;
import br.com.geospot.api.mappers.UserMapper;
import br.com.geospot.api.models.LoginRequest;
import br.com.geospot.api.models.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        var user = userMapper.toUser(request);
        var userOptional = userRepository.findByEmail(user.getEmail());
        if (userOptional.isEmpty()) {
            return null;
        }
        var isMatched = passwordEncoder.matches(request.password(), user.getPassword());
        if (!isMatched) {
            return null;
        }
        var response = userOptional.get();
        return new LoginResponse(response.getEmail(), "");
    }
}
