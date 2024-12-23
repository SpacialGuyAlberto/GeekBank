package com.geekbank.bank.controllers;

import com.geekbank.bank.services.JwtBlacklistService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/basic")
public class LogoutController {

    private final JwtBlacklistService jwtBlacklistService;

    public LogoutController(JwtBlacklistService jwtBlacklistService) {
        this.jwtBlacklistService = jwtBlacklistService;
    }

//    @PostMapping("/logout")
//    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
//        if (token != null && token.startsWith("Bearer ")) {
//            String jwt = token.substring(7);
//            jwtBlacklistService.blacklistToken(jwt);
//            SecurityContextHolder.clearContext();
//            return ResponseEntity.ok("Logged out successfully");
//        } else {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token");
//        }
//    }
//
//    @PostMapping("/logout")
//    public ResponseEntity<Void> logout(HttpServletResponse response) {
//        Cookie jwtCookie = new Cookie("jwtToken", null);
//        jwtCookie.setHttpOnly(true);
//        jwtCookie.setSecure(false);
//        jwtCookie.setPath("/");
//        jwtCookie.setMaxAge(0); // Eliminar la cookie
//        response.addCookie(jwtCookie);
//        return ResponseEntity.ok().build();
//    }


}
