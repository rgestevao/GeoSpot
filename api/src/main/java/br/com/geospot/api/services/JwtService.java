package br.com.geospot.api.services;

import br.com.geospot.api.db.User;
import br.com.geospot.api.db.UserRepository;
import br.com.geospot.api.db.UserStatusEnum;
import br.com.geospot.api.exceptions.ErrorCodeEnum;
import br.com.geospot.api.exceptions.FlowException;
import br.com.geospot.api.models.LoginResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final SecretKey key;
    private final UserRepository userRepository;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            UserRepository userRepository
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.userRepository = userRepository;
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception _) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(key)
                .compact();
    }

    public LoginResponse extractRefreshToken(String refreshToken) {
        if (!isTokenValid(refreshToken)) {
            throw new FlowException(
                    ErrorCodeEnum.BAD_REQUEST,
                    "Invalid token"
            );
        }
        String email = extractEmail(refreshToken);
        var user = userRepository.findByEmail(email).orElseThrow(
                () -> new FlowException(ErrorCodeEnum.BAD_REQUEST, "User not found"));
        if (user.getStatus() == UserStatusEnum.INACTIVE) {
            throw new FlowException(ErrorCodeEnum.BAD_REQUEST, "User is not active");
        }
        String newAccessToken = generateToken(user);
        return new LoginResponse(email, newAccessToken, refreshToken);
    }
}
