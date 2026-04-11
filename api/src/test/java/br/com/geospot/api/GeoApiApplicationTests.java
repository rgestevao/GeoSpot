package br.com.geospot.api;

import br.com.geospot.api.services.AuthService;
import br.com.geospot.api.services.JwtService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class GeoApiApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Test
    void contextLoads() {
        Assertions.assertThat(authService).isNotNull();
        Assertions.assertThat(jwtService).isNotNull();
    }
}
