package br.com.geospot.api.controllers;

import br.com.geospot.api.models.CreateUserRequest;
import br.com.geospot.api.models.CreateUserResponse;
import br.com.geospot.api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "API User Management")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "User registration flow")
    public ResponseEntity<CreateUserResponse> login(@RequestBody CreateUserRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(loginRequest));
    }
}
