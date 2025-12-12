package controller;

import dto.AuthResponse;
import dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import security.JwtUtil;
import service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService users;
    private final JwtUtil jwt;

    public AuthController(UserService users, JwtUtil jwt) {
        this.users = users;
        this.jwt = jwt;

        // создаём демо-пользователя
        users.findByEmail("user@example.com")
                .orElseGet(() -> users.createUser(
                        "DemoUser", "user@example.com", "password"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        return users.findByEmail(req.getEmail())
                .map(u -> {
                    if (users.checkPassword(req.getPassword(), u.getPasswordHash())) {
                        return ResponseEntity.ok(new AuthResponse(jwt.generateToken(u.getEmail())));
                    }
                    return ResponseEntity.status(401).build();
                })
                .orElse(ResponseEntity.status(401).build());
    }
}