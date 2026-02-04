package com.r2s.auth.service;

public interface PasswordService {
    String encodePassword(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
