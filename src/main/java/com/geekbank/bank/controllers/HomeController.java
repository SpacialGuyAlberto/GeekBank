package com.geekbank.bank.controllers;

import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Order(1)
@Controller
@RequestMapping("/api/home")
public class HomeController {

    @GetMapping
    public ResponseEntity<String> home(@AuthenticationPrincipal OidcUser oidcUser, Authentication authentication) {
        if (oidcUser != null) {
            return ResponseEntity.ok("Welcome to the home page, " + oidcUser.getEmail() + "!");
        } else if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok("Welcome to the home page, " + authentication.getName() + "!");
        } else {
            return ResponseEntity.status(401).body("Unauthorized access");
        }
    }
}
