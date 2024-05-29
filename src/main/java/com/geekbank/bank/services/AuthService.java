package com.geekbank.bank.services;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public boolean authenticate(String username, String password) {
        // Lógica de autenticación aquí
        // Puedes acceder a la base de datos para verificar las credenciales del usuario, por ejemplo
        return false; // retorna true si la autenticación es exitosa, de lo contrario false
    }
}
