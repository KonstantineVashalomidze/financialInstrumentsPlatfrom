package org.devexperts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.devexperts.RR.AuthResponse;
import org.devexperts.RR.LoginRequest;
import org.devexperts.model.User;
import org.devexperts.service.UserService;
import org.devexperts.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final String myLog = "[!!!MY_LOG!!!]";
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;

    /**
     * Will be triggered upon client reaches the endpoint, will register user with following 'LoginRequest' if it's
     * valid otherwise will response accordingly.
     *
     * @param registerRequest Object similar to .json { username: [actual user name], password: [actual password] }
     * @return success or fail messages, indicating successful registration or already existing user with username
     */
    /* curl -X POST http://localhost:8081/api/auth/register -H "Content-Type: application/json" -d "{\"username\":\"newuser\", \"password\":\"password123\"}" */
    @Operation(summary = "Register a new user", description = "Register a new user with the provided username and password")
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Username already exists")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest registerRequest) {
        String username = registerRequest.getUsername();
        String password = registerRequest.getPassword();

        logger.info("{} Registering user {} with password {}", myLog, username, password);


        if (userService.getUserByUsername(username) != null) // TODO: check null or ""
        {
            logger.info("{} User {} already exists", myLog, username);
            return ResponseEntity.badRequest()
                    .body("Username already exists");
        }

        String hashedPassword = Base64.getEncoder().encodeToString(password.getBytes());
        logger.info("{} Hashed password {}", myLog, hashedPassword);
        userService.createUser(
                new User(username, hashedPassword, new ArrayList<>())
        );

        logger.info("{} User {} registered successfully", myLog, username);
        return ResponseEntity.ok("User registered successfully");
    }

    /**
     * Handles user login request, once successful login json web token will be generated and sent back to the client,
     * if invalid credentials were provided initially, it will response accordingly.
     *
     * @param loginRequest Object similar to .json { username: [actual user name], password: [actual password] }
     * @return success or fail messages, indicating successful authentication or invalid credentials
     */
    /* curl -X POST http://localhost:8081/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"newuser\", \"password\":\"password123\"}" */
    @Operation(summary = "Login user", description = "Authenticate user and generate JWT token")
    @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        logger.info("{} Authenticating user {} with password {}", myLog, username, password);

        if (isValidCredentials(
                username,
                password
        )) {
            String token = jwtUtil.generateToken(username);
            logger.info("{} Created JWT token {} for user {}", myLog, token, username);
            return ResponseEntity.ok(new AuthResponse(token));
        }
        logger.info("{} Invalid credentials {} for user {}", myLog, password, username);
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
            logger.info("{} User {} not found", myLog, username);
            return false;
        }

        /* User was found */
        logger.info("{} User {} authenticated successfully", myLog, username);

        byte[] decodedBytes = Base64.getDecoder().decode(user.getPassword());
        String decodedPassword = new String(decodedBytes);

        logger.info("{} Decoded password {}", myLog, decodedPassword);

        return decodedPassword.equals(password);
    }


}