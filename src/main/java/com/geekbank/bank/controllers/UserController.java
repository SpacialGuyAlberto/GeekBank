package com.geekbank.bank.controllers;

import com.geekbank.bank.util.JwtTokenUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.RestController;
import com.geekbank.bank.models.User;
import com.geekbank.bank.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtDecoder jwtDecoder;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, JwtDecoder jwtDecoder, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.jwtDecoder = jwtDecoder;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Optional<User>> getUserById(@PathVariable long userId) {
        Optional<User> user = userService.getUserById(userId);
        return user.isPresent() ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        userService.registerUser(user);
        return ResponseEntity.created(URI.create("/api/users/" + user.getId())).body(user);
    }

    @GetMapping("/user-details")
    public ResponseEntity<User> getUserDetails(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String email = authentication.getName();
        Optional<User> user = userService.findByEmail(email);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/update-user-details")
    public ResponseEntity<Map<String, String>> updateUserDetails(@RequestBody userInfoRequest userInfoRequest){
        try {
            // Autenticar al usuario
            Authentication authenticationRequest =
                    new UsernamePasswordAuthenticationToken(userInfoRequest.getEmail(), userInfoRequest.getPassword());
            Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);

            // Obtener los detalles del usuario autenticado
            UserDetails userDetails = (UserDetails) authenticationResponse.getPrincipal();
            User user = userService.findByEmail(userInfoRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Verificar si el usuario está habilitado
            if (!user.isEnabled()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Usuario no habilitado"));
            }

            // Actualizar los detalles del usuario
            if (!userInfoRequest.getEmail().equals(user.getEmail())) {
                user.setEmail(userInfoRequest.getEmail());
            }
            if (userInfoRequest.getName() != null && !userInfoRequest.getName().isEmpty()) {
                user.setName(userInfoRequest.getName());
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "El nombre no puede estar vacío"));
            }
            if (userInfoRequest.getPhoneNumber() != null && !userInfoRequest.getPhoneNumber().isEmpty()) {
                user.setPhoneNumber(userInfoRequest.getPhoneNumber());
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "El número de teléfono no puede estar vacío"));
            }

            // Guardar los cambios
            userService.updateUser(user);
            String jwtToken = jwtTokenUtil.generateToken(userDetails);

            // Preparar la respuesta
            Map<String, String> response = new HashMap<>();
            response.put("token", jwtToken);
            response.put("userId", String.valueOf(user.getId()));

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Error de autenticación: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Error al encontrar usuario: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno al actualizar los detalles del usuario: " + e.getMessage()));
        }
    }


    public static class userInfoRequest {
        private String name;
        private String email;
        private String password;
        private String phoneNumber;


        public String getName(){ return this.name; }
        public void setName(String name){
            this.name = name;
        }

        public String getEmail(){ return email; }
        public void setEmail(String email ){ this.email = email; }

        public String getPhoneNumber(){ return this.phoneNumber; }
        public String getPassword(){return this.password; }

        public void setPhoneNumber(String phoneNumber ){
            this.phoneNumber = phoneNumber;
        }

    }


}
