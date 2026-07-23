package com.bookmarks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@RequestBody UserRegistrationRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().length() < 3 || request.getUsername().trim().length() > 50) {
            throw new BadRequestException("Username must be between 3 and 50 characters");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6 || request.getPassword().length() > 100) {
            throw new BadRequestException("Password must be between 6 and 100 characters");
        }

        String username = request.getUsername().trim();
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new BadRequestException("Username must contain only alphanumeric characters and underscores");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new BadRequestException("Username already exists");
        }

        String passwordHash = PasswordUtil.hashPassword(request.getPassword());
        User user = new User(username, passwordHash);
        userRepository.save(user);

        return new ResponseEntity<>(new MessageResponse("User registered successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody UserLoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new UnauthorizedException("Username and password are required");
        }

        User user = userRepository.findByUsername(request.getUsername().trim())
            .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!PasswordUtil.verifyPassword(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        String token = JwtUtil.generateToken(user.getUsername());
        return ResponseEntity.ok(new TokenResponse(token, "Bearer"));
    }
}
