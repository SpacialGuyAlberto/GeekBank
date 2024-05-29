package com.geekbank.bank.controllers;

import com.geekbank.bank.models.User;
import com.geekbank.bank.services.UserService;
import com.geekbank.bank.util.JwtTokenUtil;
import com.geekbank.bank.models.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Order(1)
@RestController
@RequestMapping("/api/auth")
public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public RegisterController(AuthenticationManager authenticationManager, UserService userService, PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/registerUser")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        logger.info("Iniciando registro para: {}", registerRequest.getEmail());

        if (registerRequest.getEmail() == null || registerRequest.getPassword() == null) {
            logger.error("Email o contraseña no proporcionados");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email y contraseña son requeridos");
        }

        Optional<User> existingUser = userService.findByEmail(registerRequest.getEmail());
        if (existingUser.isPresent()) {
            logger.error("El usuario ya existe: {}", registerRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe");
        }

        User newUser = new User();
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setName(registerRequest.getName());
        newUser.setRole("USER");
        newUser.setEnabled(true);

        User createdUser = userService.createUser(newUser);
        logger.info("Usuario creado: {}", createdUser.getEmail());

        Authentication authenticationRequest =
                new UsernamePasswordAuthenticationToken(createdUser.getEmail(), registerRequest.getPassword());
        try {
            logger.info("Autenticando usuario: {}", createdUser.getEmail());
            Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);
            String jwtToken = jwtTokenUtil.generateToken(new UserDetailsImpl(createdUser));
            logger.info("Usuario autenticado: {}", createdUser.getEmail());
            return ResponseEntity.ok("Usuario registrado correctamente. Token JWT: " + jwtToken);
        } catch (BadCredentialsException e) {
            logger.error("Error al autenticar al usuario después del registro", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al autenticar al usuario después del registro");
        }
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        logger.error("Error en el registro: {}", ex.getReason(), ex);
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
    }

    public static class RegisterRequest {
        private String email;
        private String password;
        private String name;

        public RegisterRequest() {}

        public RegisterRequest(String email, String password, String name) {
            this.email = email;
            this.password = password;
            this.name = name;
        }

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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
