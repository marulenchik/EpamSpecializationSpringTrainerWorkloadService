package com.example.trainerworkloadservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;

@Component
public class JwtTokenValidator {

    private final String secret;
    private final String issuer;
    private SecretKey signingKey;

    public JwtTokenValidator(@Value("${jwt.secret}") String secret,
                             @Value("${jwt.issuer}") String issuer) {
        this.secret = secret;
        this.issuer = issuer;
    }

    @PostConstruct
    public void init() {
        byte[] decoded = Base64.getDecoder().decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(decoded);
    }

    public Claims validate(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

