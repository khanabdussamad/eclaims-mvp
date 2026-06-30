package com.nagarro.eclaims.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${security.jwt.secret:change-this-secret-for-local-only}")
    private String jwtSecret;

    @Value("${security.jwt.access-token-expiration-minutes:60}")
    private long accessTokenExpirationMinutes;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UUID userId, String email, Set<String> roles, Set<String> permissions) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("email", email)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMinutes * 60 * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUserIdFromToken(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    public String getEmailFromToken(String token) {
        return extractClaim(token, claims -> claims.getSubject());
    }

    @SuppressWarnings("unchecked")
    public Set<String> getRolesFromToken(String token) {
        return extractClaim(token, claims -> (Set<String>) claims.get("roles"));
    }

    @SuppressWarnings("unchecked")
    public Set<String> getPermissionsFromToken(String token) {
        return extractClaim(token, claims -> (Set<String>) claims.get("permissions"));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public long getExpirationTime() {
        return accessTokenExpirationMinutes * 60;
    }

    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

