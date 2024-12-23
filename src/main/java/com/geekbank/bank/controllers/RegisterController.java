package com.geekbank.bank.controllers;

import com.geekbank.bank.models.Roles;
import com.geekbank.bank.models.User;
import com.geekbank.bank.services.UserService;
import com.geekbank.bank.services.SendGridEmailService;
import com.geekbank.bank.util.JwtTokenUtil;
import com.geekbank.bank.models.UserDetailsImpl;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Order(1)
@CrossOrigin(origins = "${DOMAIN_ORIGIN_URL}")
@RestController
@RequestMapping("/auth")
public class RegisterController {
    private static final Logger logger = LoggerFactory
            .getLogger(RegisterController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private  Roles roles;
    private final SendGridEmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public RegisterController(AuthenticationManager authenticationManager, UserService userService, SendGridEmailService emailService, PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/registerUser")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        logger.info("Iniciando registro para: {}", registerRequest.getEmail());
        logger.info("Contrasena proveida : {}", registerRequest.getPassword());

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
        newUser.setRole(Roles.CUSTOMER);

        userService.registerUser(newUser);
        logger.info("Usuario creado: {}", newUser.getEmail());
        logger.info("Contrasena asignada: {}", newUser.getPassword());

        return ResponseEntity.ok("Usuario registrado correctamente. Por favor, revisa tu email para activar la cuenta.");
    }

    @PostMapping("/registerUserByAdmin")
    public ResponseEntity<String> registerUserByAdmin(@RequestBody RegisterRequest registerRequest) {
        logger.info("Administrador iniciando registro para: {}", registerRequest.getEmail());

        if (registerRequest.getEmail() == null || registerRequest.getName() == null) {
            logger.error("Email o nombre no proporcionados");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email y nombre son requeridos");
        }

        Optional<User> existingUser = userService.findByEmail(registerRequest.getEmail());
        if (existingUser.isPresent()) {
            logger.error("El usuario ya existe: {}", registerRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe");
        }

        User newUser = new User();
        newUser.setEmail(registerRequest.getEmail());
        newUser.setName(registerRequest.getName());
        newUser.setRole(Roles.CUSTOMER);
        newUser.setEnabled(false);

        String activationToken = UUID.randomUUID().toString();
        newUser.setActivationToken(activationToken);

        userService.registerUserByAdmin(newUser);
        logger.info("Usuario creado por admin: {}", newUser.getEmail());

        emailService.sendSetPasswordEmail(newUser);

        return ResponseEntity.ok("Usuario registrado correctamente. Se ha enviado un correo electrónico para que el usuario establezca su contraseña.");
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateUser(@RequestParam("token") String token) {
        logger.info("Activating user with token: {}", token);
        boolean isActivated = userService.activateUser(token);
        if (isActivated) {
            return ResponseEntity.ok("User activated successfully.");
        } else {
            return ResponseEntity.badRequest().body("Invalid activation token.");
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

        public RegisterRequest(String email, String name) {
            this.email = email;
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            logger.info(password);
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
