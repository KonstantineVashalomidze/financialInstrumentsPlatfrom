package org.devexperts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.devexperts.RR.AuthResponse;
import org.devexperts.RR.LoginRequest;
import org.devexperts.RR.RegisterRequest;
import org.devexperts.model.User;
import org.devexperts.service.UserService;
import org.devexperts.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Base64;

/**
 * @author Konstantine Vashalomidze
 * <p>
 * Controller responsible for registering and logging user.
 * Swagger: http://localhost:8081/swagger-ui/index.html#/
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;

    @Operation(summary = "Register a new user", description = "Register a new user with the provided username and password")
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Username already exists")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        String username = registerRequest.getUsername();
        String password = registerRequest.getPassword();


        if (userService.getUserByUsername(username) != null) {
            return ResponseEntity.badRequest()
                    .body("Username already exists");
        }

        String hashedPassword = Base64.getEncoder().encodeToString(password.getBytes());
        userService.createUser(
                new User(username, hashedPassword, new ArrayList<>())
        );

        return ResponseEntity.ok("User registered successfully");
    }

    @Operation(summary = "Login user", description = "Authenticate user and generate JWT token")
    @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();


        if (isValidCredentials(
                username,
                password
        )) {
            String token = jwtUtil.generateToken(username);
            return ResponseEntity.ok(new AuthResponse(token));
        }
        return ResponseEntity.badRequest()
                .body("Invalid credentials");
    }

    private boolean isValidCredentials(
            String username,
            String password
    ) {

        User user = userService.getUserByUsername(username);
        if (user == null) // Such user not found in database
        {
            return false;
        }

        /* User was found */

        byte[] decodedBytes = Base64.getDecoder().decode(user.getPassword());
        String decodedPassword = new String(decodedBytes);


        return decodedPassword.equals(password);
    }


}