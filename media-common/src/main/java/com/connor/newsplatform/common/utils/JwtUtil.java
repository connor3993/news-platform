package com.connor.newsplatform.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public final class JwtUtil {
    private JwtUtil() {
    }

    public static String createJwt(String secretKey, long ttlMillis, Map<String, Object> claims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + ttlMillis);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key(secretKey))
                .compact();
    }

    public static Claims parseJwt(String secretKey, String token) {
        return Jwts.parser()
                .verifyWith(key(secretKey))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static SecretKey key(String secretKey) {
        byte[] bytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
