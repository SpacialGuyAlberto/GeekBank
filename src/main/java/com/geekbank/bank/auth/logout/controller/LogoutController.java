package com.geekbank.bank.auth.logout.controller;

import com.geekbank.bank.auth.JwtBlacklistService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/basic")
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
