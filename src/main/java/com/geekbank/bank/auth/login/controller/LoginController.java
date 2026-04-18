package com.geekbank.bank.auth.login.controller;

import com.geekbank.bank.auth.login.dto.LoginRequest;
import com.geekbank.bank.auth.login.dto.ResetPasswordRequest;
import com.geekbank.bank.auth.service.AuthService;
import com.geekbank.bank.core.controller.BaseController;
import com.geekbank.bank.core.response.ApiResponse;
import com.geekbank.bank.core.security.SecurityConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Order(1)
@RestController
@RequestMapping("/api/auth")
public class LoginController extends BaseController {

    private final AuthService authService;

    @Autowired
    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        Map<String, String> authResponse = authService.login(loginRequest);
        String jwtToken = authResponse.get("token");
        authResponse.remove("token");

        createJwtCookie(response, jwtToken);



        return success(authResponse);
    }

    @GetMapping("/check-auth")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        Map<String, Boolean> response = new HashMap<>();
        response.put("authenticated", isAuthenticated);
        return success(response);
    }

    @PostMapping("/validate-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> validatePassword(
            @Valid @RequestBody LoginRequest loginRequest) {
        return success(authService.validatePassword(loginRequest));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        return success(authService.resetPassword(resetPasswordRequest));
    }

    @GetMapping("/login")
    public ResponseEntity<String> getLoginInfo() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("El método GET no está permitido para /login");
    }

    @PostMapping("/google-login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> googleLogin(@RequestBody Map<String, String> tokenData,
            HttpServletResponse response) {
        String token = tokenData.get("token");
        Map<String, Object> authResponse = authService.googleLogin(token);
        String jwtToken = (String) authResponse.get("token");

        createJwtCookie(response, jwtToken);

        return success(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie(SecurityConstants.JWT_COOKIE_NAME, null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath(SecurityConstants.COOKIE_PATH);
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        return ResponseEntity.ok().build();
    }

    private void createJwtCookie(HttpServletResponse response, String jwtToken) {
        ResponseCookie jwtCookie = ResponseCookie.from(SecurityConstants.JWT_COOKIE_NAME, jwtToken)
                .httpOnly(true)
                .secure(false)
                .path(SecurityConstants.COOKIE_PATH)
                .maxAge(3600)
                .sameSite(SecurityConstants.SAME_SITE)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
    }
}
