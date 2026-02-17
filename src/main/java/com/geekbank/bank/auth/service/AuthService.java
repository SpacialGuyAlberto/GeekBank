package com.geekbank.bank.auth.service;

import com.geekbank.bank.auth.login.dto.LoginRequest;
import com.geekbank.bank.auth.login.dto.ResetPasswordRequest;
import java.util.Map;

public interface AuthService {
    Map<String, String> login(LoginRequest loginRequest);

    Map<String, String> validatePassword(LoginRequest loginRequest);

    Map<String, String> resetPassword(ResetPasswordRequest resetPasswordRequest);

    Map<String, Object> googleLogin(String token);
}
