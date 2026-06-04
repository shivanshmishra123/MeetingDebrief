package com.example.demo.service;

import com.example.demo.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles all JWT operations: generation, parsing, and validation.
 *
 * Algorithm : HS256 (HMAC-SHA256) — symmetric, single secret key.
 * Expiry     : 30 days (configured via jwt.expiration-ms in application.properties).
 *
 * Token Payload (claims):
 *   sub   — user's email (unique identifier)
 *   name  — display name
 *   desig — company designation / job title
 *   iat   — issued-at timestamp
 *   exp   — expiry timestamp (iat + 30 days)
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    // ------------------------------------------------------------------
    // Token Generation
    // ------------------------------------------------------------------

    /**
     * Creates a signed JWT for the given user.
     * Embeds name and designation as extra claims so the frontend
     * can decode them without a round-trip to the server.
     */
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("name", user.getName());
        extraClaims.put("desig", user.getDesignation());

        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getEmail())          // sub = email
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // ------------------------------------------------------------------
    // Token Validation
    // ------------------------------------------------------------------

    /**
     * Returns true if the token is signed correctly, not expired,
     * and the subject (email) matches the UserDetails provided.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // ------------------------------------------------------------------
    // Claims Extraction helpers
    // ------------------------------------------------------------------

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractName(String token) {
        return extractClaim(token, claims -> claims.get("name", String.class));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
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

    // ------------------------------------------------------------------
    // Key
    // ------------------------------------------------------------------

    /**
     * Derives an HMAC-SHA key from the configured secret string.
     * Keys.hmacShaKeyFor() ensures the key meets the HS256 minimum.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
