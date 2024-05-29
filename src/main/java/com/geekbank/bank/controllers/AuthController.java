package com.geekbank.bank.controllers;

import com.geekbank.bank.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

//    @PostMapping("/login")
//    public String login(@RequestBody LoginRequest request) {
//
//        boolean isAuthenticated = authService.authenticate(request.getUsername(), request.getPassword());
//        if (isAuthenticated) {
//            return "Login exitoso";
//        } else {
//            return "Credenciales inválidas";
//        }
//    }

    static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}


