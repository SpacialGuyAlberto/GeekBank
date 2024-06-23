package com.geekbank.bank.controllers;

import com.geekbank.bank.models.User;
import com.geekbank.bank.models.UserDetailsImpl;
import com.geekbank.bank.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import com.geekbank.bank.util.JwtTokenUtil;

import java.util.HashMap;
import java.util.Map;

@Order(1)
@RestController
@RequestMapping("/api/auth")
public class LoginController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final JwtDecoder jwtDecoder;

    @Autowired
    public LoginController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, UserService userService, JwtDecoder jwtDecoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
        this.jwtDecoder = jwtDecoder;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        Authentication authenticationRequest =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        try {
            Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);
            UserDetails userDetails = (UserDetails) authenticationResponse.getPrincipal();
            String jwtToken = jwtTokenUtil.generateToken(userDetails);

            User user = userService.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Map<String, String> response = new HashMap<>();
            response.put("token", jwtToken);
            response.put("userId", String.valueOf(user.getId()));
            return (ResponseEntity<Map<String, String>>) ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales invalidas"));
        }
    }

    @GetMapping("/login")
    public ResponseEntity<String> getLoginInfo() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("El método GET no está permitido para /login");
    }

@PostMapping("/google-login")
public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> tokenData) {
    String token = tokenData.get("token");
    Jwt decodedToken = jwtDecoder.decode(token);

    String email = decodedToken.getClaim("email");
    User user = userService.findByEmail(email).orElseGet(() -> {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(decodedToken.getClaim("name"));
        newUser.setPassword("");
        userService.createUser(newUser);
        return newUser;
    });

    UserDetailsImpl userDetails = new UserDetailsImpl(user);
    String jwtToken = jwtTokenUtil.generateToken(userDetails);
    Map<String, Object> response = new HashMap<>();
    response.put("token", jwtToken);
    response.put("userId", user.getId());
    return ResponseEntity.ok(response);
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
