package com.geekbank.bank.auth.service;

import com.geekbank.bank.auth.login.dto.LoginRequest;
import com.geekbank.bank.auth.login.dto.ResetPasswordRequest;
import com.geekbank.bank.common.exceptions.ResourceNotFoundException;
import com.geekbank.bank.user.model.User;
import com.geekbank.bank.user.UserDetailsImpl;
import com.geekbank.bank.user.service.UserService;
import com.geekbank.bank.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final JwtDecoder jwtDecoder;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil,
            UserService userService, JwtDecoder jwtDecoder, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
        this.jwtDecoder = jwtDecoder;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Map<String, String> login(LoginRequest loginRequest) {
        Authentication authenticationRequest = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
                loginRequest.getPassword());

        Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);
        UserDetails userDetails = (UserDetails) authenticationResponse.getPrincipal();
        String jwtToken = jwtTokenUtil.generateToken(userDetails);

        User user = userService.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("userId", String.valueOf(user.getId()));
        responseBody.put("token", jwtToken); // Sending token back to controller to handle cookie/header if needed
        return responseBody;
    }

    @Override
    public Map<String, String> validatePassword(LoginRequest loginRequest) {
        Authentication authenticationRequest = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
                loginRequest.getPassword());

        Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);
        UserDetails userDetails = (UserDetails) authenticationResponse.getPrincipal();
        String jwtToken = jwtTokenUtil.generateToken(userDetails);

        User user = userService.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Map<String, String> response = new HashMap<>();
        response.put("token", jwtToken);
        response.put("userId", String.valueOf(user.getId()));

        return response;
    }

    @Override
    public Map<String, String> resetPassword(ResetPasswordRequest resetPasswordRequest) {
        Authentication authenticationRequest = new UsernamePasswordAuthenticationToken(resetPasswordRequest.getEmail(),
                resetPasswordRequest.getOldPassword());
        Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);

        UserDetails userDetails = (UserDetails) authenticationResponse.getPrincipal();
        User user = userService.findByEmail(resetPasswordRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Usuario no habilitado");
        }

        if (passwordEncoder.matches(resetPasswordRequest.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("La nueva contraseña no puede ser igual a la anterior");
        }

        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        userService.save(user); // Updated method name

        String jwtToken = jwtTokenUtil.generateToken(userDetails);

        Map<String, String> response = new HashMap<>();
        response.put("token", jwtToken);
        response.put("userId", String.valueOf(user.getId()));

        return response;
    }

    @Override
    public Map<String, Object> googleLogin(String token) {
        Jwt decodedToken = jwtDecoder.decode(token);

        String email = decodedToken.getClaim("email");
        User user = userService.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(decodedToken.getClaim("name"));
            newUser.setPassword("");
            userService.save(newUser); // Updated method name
            return newUser;
        });

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        String jwtToken = jwtTokenUtil.generateToken(userDetails);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("userId", user.getId());
        responseBody.put("token", jwtToken);
        return responseBody;
    }
}
