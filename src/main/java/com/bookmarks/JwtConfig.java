package com.bookmarks;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtConfig {

    @Value("${jwt.secret:extremely_long_and_very_secure_secret_key_that_is_at_least_256_bits_long_for_hmac_sha256}")
    private String secret;

    @PostConstruct
    public void init() {
        JwtUtil.setSecret(secret);
    }
}
