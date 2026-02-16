package org.watts.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.watts.security.jwt.JwtUtils;
import org.watts.security.user.dto.LoginRequest;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;

    public AuthController(
            AuthenticationManager authManager,
            JwtUtils jwtUtils
    ) {
        this.authManager = authManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest usuario) {
        // u.getEmail() y u.getPasswordHash() se usan para login
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        usuario.getUsername(),
                        usuario.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();

        String token = jwtUtils.generateToken(user);

        // Devuelve un JSON simple con el token
        return Map.of("token", token);
    }
}
