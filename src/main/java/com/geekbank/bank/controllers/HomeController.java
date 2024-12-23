package com.geekbank.bank.controllers;

import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Order(1)
@Controller
@RequestMapping("/home")
public class HomeController {

    @GetMapping
    public ResponseEntity<?> home(@AuthenticationPrincipal OidcUser oidcUser, Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        if (oidcUser != null) {
            response.put("message", "Welcome to the home page, " + oidcUser.getEmail() + "!");
            return ResponseEntity.ok(response);
        } else if (authentication != null && authentication.isAuthenticated()) {
            response.put("message", "Welcome to the home page, " + authentication.getName() + "!");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Unauthorized access");
            return ResponseEntity.status(401).body(response);
        }
    }
}
