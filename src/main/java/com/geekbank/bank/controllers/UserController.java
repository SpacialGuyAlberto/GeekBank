package com.geekbank.bank.controllers;

import com.geekbank.bank.models.Account;
import com.geekbank.bank.models.VerificationStatus;
import com.geekbank.bank.repositories.AccountRepository;
import com.geekbank.bank.util.JwtTokenUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.RestController;
import com.geekbank.bank.models.User;
import com.geekbank.bank.services.UserService;
import com.geekbank.bank.services.AccountService;
import com.geekbank.bank.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final AccountService accountService;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtDecoder jwtDecoder;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService,
                          AuthenticationManager authenticationManager, AccountService accountService,
                          JwtTokenUtil jwtTokenUtil,
                          JwtDecoder jwtDecoder,
                          PasswordEncoder passwordEncoder,
                          UserRepository userRepository) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.accountService = accountService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.jwtDecoder = jwtDecoder;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Optional<User>> getUserById(@PathVariable long userId) {
        Optional<User> user = userService.getUserById(userId);
        return user.isPresent() ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    @GetMapping("/checkUser")
    public ResponseEntity<Map<String, Boolean>> checkIfUserExists(@RequestParam String email) {
        boolean exists = userService.findByEmail(email).isPresent();
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
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
            Authentication authenticationRequest =
                    new UsernamePasswordAuthenticationToken(userInfoRequest.getEmail(), userInfoRequest.getPassword());
            Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);

            UserDetails userDetails = (UserDetails) authenticationResponse.getPrincipal();
            User user = userService.findByEmail(userInfoRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!user.isEnabled()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Usuario no habilitado"));
            }

            if (!userInfoRequest.getEmail().equals(user.getEmail())) {
                user.setEmail(userInfoRequest.getEmail());
            }
            if (userInfoRequest.getName() != null && !userInfoRequest.getName().isEmpty()) {
                user.setName(userInfoRequest.getName());
            }
            if (userInfoRequest.getPhoneNumber() != null && !userInfoRequest.getPhoneNumber().isEmpty()) {
                user.setPhoneNumber(userInfoRequest.getPhoneNumber());
            }

            userService.updateUser(user);
            String jwtToken = jwtTokenUtil.generateToken(userDetails);

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

    @PostMapping("/setPassword")
    public ResponseEntity<String> setPassword(@RequestBody SetPasswordRequest request) {
        String token = request.getToken();
        String newPassword = request.getPassword();

        Optional<User> userOptional = userRepository.findByActivationToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Optional<Account> optionalAccount = accountService.getAccountsByUserId(user.getId());
            if (!optionalAccount.isPresent()){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            }
            Account account = optionalAccount.get();
            accountService.changeVerificationStatus(account.getId(), VerificationStatus.VERIFIED);
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setEnabled(true);
            user.setActivationToken(null);
            userRepository.save(user);

            return ResponseEntity.ok("Contraseña establecida correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token inválido o expirado.");
        }
    }

    public static class SetPasswordRequest {
        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        private String token;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        private String password;

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

        public void setPassword(String password) {
            this.password = password;
        }
    }


}
