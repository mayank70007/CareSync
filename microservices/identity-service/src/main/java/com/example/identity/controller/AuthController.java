package com.example.identity.controller;

import com.example.identity.dto.LoginRequest;
import com.example.identity.dto.LoginResponse;
import com.example.identity.dto.RegisterRequest;
import com.example.identity.model.User;
import com.example.identity.repository.UserRepository;
import com.example.identity.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        if (auth.isAuthenticated()) {
            User dbUser = userRepository.findByUsername(loginRequest.getUsername()).get();
            String token = jwtUtil.generateToken(dbUser.getUsername(), dbUser.getRole());
            return ResponseEntity.ok(new LoginResponse(token, dbUser.getRole()));
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req){
        if(req.getUsername()==null || req.getUsername().isBlank()) return ResponseEntity.badRequest().body("username required");
        if(req.getPassword()==null || req.getPassword().isBlank()) return ResponseEntity.badRequest().body("password required");
        if(req.getRole()==null || req.getRole().isBlank()) return ResponseEntity.badRequest().body("role required");
        if(userRepository.findByUsername(req.getUsername()).isPresent()){
            return ResponseEntity.status(409).body("username already exists");
        }
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole(req.getRole());
        u = userRepository.save(u);
        return ResponseEntity.ok(u);
    }
}
