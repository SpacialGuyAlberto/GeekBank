package com.geekbank.bank.controllers;

import com.geekbank.bank.models.User;
import com.geekbank.bank.models.UserDetailsImpl;
import com.geekbank.bank.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import com.geekbank.bank.util.JwtTokenUtil;
import jakarta.servlet.http.Cookie;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Order(1)
@RestController
@RequestMapping("/api/auth")
public class LoginController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final JwtDecoder jwtDecoder;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public LoginController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, UserService userService, JwtDecoder jwtDecoder, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
        this.jwtDecoder = jwtDecoder;
        this.passwordEncoder = passwordEncoder;
    }

//    @PostMapping("/login")
//    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
//        Authentication authenticationRequest =
//                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
//        try {
//            Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);
//            UserDetails userDetails = (UserDetails) authenticationResponse.getPrincipal();
//            String jwtToken = jwtTokenUtil.generateToken(userDetails);
//
//            User user = userService.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
//
//
//
//
//            Map<String, String> response = new HashMap<>();
//            response.put("token", jwtToken);
//            response.put("userId", String.valueOf(user.getId()));
//
//            return (ResponseEntity<Map<String, String>>) ResponseEntity.ok(response);
//        } catch (BadCredentialsException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales invalidas"));
//        }
//    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Authentication authenticationRequest =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        try {
            Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);
            UserDetails userDetails = (UserDetails) authenticationResponse.getPrincipal();
            String jwtToken = jwtTokenUtil.generateToken(userDetails);

            User user = userService.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", jwtToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("Lax")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("userId", String.valueOf(user.getId()));
            return ResponseEntity.ok(responseBody);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales inválidas"));
        }
    }

    @GetMapping("/check-auth")
    public ResponseEntity<Map<String, Boolean>> checkAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
        Map<String, Boolean> response = new HashMap<>();
        response.put("authenticated", isAuthenticated);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/validate-password")
    public ResponseEntity<Map<String, String>> validatePassword(@RequestBody LoginRequest loginRequest) {
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

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            Authentication authenticationRequest =
                    new UsernamePasswordAuthenticationToken(resetPasswordRequest.getEmail(), resetPasswordRequest.getOldPassword());
            Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);

            UserDetails userDetails = (UserDetails) authenticationResponse.getPrincipal();
            User user = userService.findByEmail(resetPasswordRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!user.isEnabled()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Usuario no habilitado"));
            }

            if (passwordEncoder.matches(resetPasswordRequest.getNewPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "La nueva contraseña no puede ser igual a la anterior"));
            }

            user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
            userService.updateUser(user);  // Asegúrate de tener este método implementado en tu UserService

            String jwtToken = jwtTokenUtil.generateToken(userDetails);

            Map<String, String> response = new HashMap<>();
            response.put("token", jwtToken);
            response.put("userId", String.valueOf(user.getId()));

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales inválidas"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ocurrió un error al restablecer la contraseña"));
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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwtToken", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Eliminar la cookie
        response.addCookie(jwtCookie);
        return ResponseEntity.ok().build();
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

    public static class ResetPasswordRequest {
        private String email;
        private String oldPassword;
        private String newPassword;

        public String getEmail(){ return email; }
        public String getOldPassword() {return oldPassword; }
        public String getNewPassword() {return newPassword; }

        public void setEmail( String email){
            this.email = email;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }
        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
