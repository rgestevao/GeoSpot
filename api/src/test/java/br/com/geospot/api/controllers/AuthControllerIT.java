package br.com.geospot.api.controllers;

import br.com.geospot.api.client.GeoapifyClient;
import br.com.geospot.api.models.GetPlacesResponse;
import br.com.geospot.api.models.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeoapifyClient geoapifyClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateUserLoginAndCallPlaces() throws Exception {
        Mockito.when(geoapifyClient.searchPlaces(
                Mockito.anyDouble(),
                Mockito.anyDouble(),
                Mockito.anyString(),
                Mockito.anyInt(),
                Mockito.anyInt()
        )).thenReturn(new GetPlacesResponse(List.of()));
        var createUserJson = """
                    {
                        "name": "Rodrigo",
                        "email": "user@test.com",
                        "password": "123456"
                    }
                """;
        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(createUserJson))
                .andExpect(MockMvcResultMatchers.status().isCreated());
        var loginJson = """
                    {
                        "email": "user@test.com",
                        "password": "123456"
                    }
                """;
        var loginResponse = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(loginJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var responseBody = loginResponse.getResponse().getContentAsString();
        var login = objectMapper.readValue(responseBody, LoginResponse.class);
        String token = login.accessToken();
        mockMvc.perform(MockMvcRequestBuilders.get("/places")
                        .param("lat", "-23.55")
                        .param("lon", "-46.63")
                        .param("category", "catering.restaurant")
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
