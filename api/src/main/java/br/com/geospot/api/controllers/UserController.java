package br.com.geospot.api.controllers;

import br.com.geospot.api.models.CreateUserRequest;
import br.com.geospot.api.models.CreateUserResponse;
import br.com.geospot.api.models.DeleteUserResponse;
import br.com.geospot.api.models.UpdatePasswordRequest;
import br.com.geospot.api.models.UpdatePasswordResponse;
import br.com.geospot.api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

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

    @DeleteMapping("/{userId}")
    @Operation(summary = "User delete flow")
    public ResponseEntity<DeleteUserResponse> login(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.deleteUser(userId));
    }

    @PutMapping("/reset-password/{userId}")
    @Operation(summary = "User update flow")
    public ResponseEntity<UpdatePasswordResponse> login(
            @PathVariable UUID userId,
            @RequestBody UpdatePasswordRequest updatePasswordRequest
    ) {
        return ResponseEntity.ok(userService.updatePassword(userId, updatePasswordRequest));
    }
}
