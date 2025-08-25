package com.effective.cards.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Service
public class GeneralJwtService {

    private final RefreshJwtService refreshJwtService;
    private final Key accessKey;
    private final Key refreshKey;

    public GeneralJwtService(@Value("${accessJwt.secret}") String accessSecret,
                             @Value("${refreshJwt.secret}") String refreshSecret,
                             RefreshJwtService refreshJwtService) {
        this.refreshJwtService = refreshJwtService;
        byte[] decodedKey = Base64.getDecoder().decode(accessSecret);
        this.accessKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");
        decodedKey = Base64.getDecoder().decode(refreshSecret);
        this.refreshKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");
    }

    public String[] generateTokens(Long userId, String phoneNumber, String role) {
        String[] tokens = new String[] {
                Jwts.builder()
                        .setSubject(userId.toString())
                        .claim("phone number", phoneNumber)
                        .claim("role", role)
                        .setIssuedAt(new Date())
                        .setExpiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
                        .signWith(accessKey)
                        .compact(),

                Jwts.builder()
                        .setSubject(userId.toString())
                        .claim("phone number", phoneNumber)
                        .claim("role", role)
                        .setIssuedAt(new Date())
                        .setExpiration(Date.from(Instant.now().plus(30, ChronoUnit.DAYS)))
                        .signWith(refreshKey)
                        .compact()
        };
        addRefreshToken(tokens[1]);
        return tokens;
    }

    public String[] refreshTokens(String token) {
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(refreshKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtException("Token expired");
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Invalid token format");
        }

        if (!refreshJwtService.isTokenExists(token)) {
            throw new JwtException("Token is not existed");
        }

        Long id = Long.parseLong(claims.getSubject());
        String phone = claims.get("phone number", String.class);
        String role = claims.get("role", String.class);

        removeRefreshToken(token);

        return generateTokens(id, phone, role);
    }

    public void addRefreshToken(String token) {
        refreshJwtService.addToken(token, getExpiration(token));
    }

    public void removeRefreshToken(String token) {
        refreshJwtService.removeToken(token);
    }

    public Long extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(accessKey).build()
                    .parseClaimsJws(token).getBody();
            return Long.parseLong(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private Date getExpiration(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(refreshKey).build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

}
