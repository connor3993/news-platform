package com.connor.newsplatform.server;

import com.connor.newsplatform.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET_KEY = "media-admin-secret-media-admin-secret";

    @Test
    void createAndParseJwt() {
        Map<String, Object> claims = Map.of(
                "userId", 100L,
                "userType", "admin",
                "username", "zhangsan"
        );
        String token = JwtUtil.createJwt(SECRET_KEY, 3600_000, claims);
        assertNotNull(token);
        assertFalse(token.isBlank());

        Claims parsed = JwtUtil.parseJwt(SECRET_KEY, token);
        assertEquals(100L, parsed.get("userId", Long.class));
        assertEquals("admin", parsed.get("userType", String.class));
        assertEquals("zhangsan", parsed.get("username", String.class));
    }

    @Test
    void expiredTokenThrowsException() {
        String token = JwtUtil.createJwt(SECRET_KEY, -1000, Map.of("userId", 1L));
        assertThrows(ExpiredJwtException.class, () -> JwtUtil.parseJwt(SECRET_KEY, token));
    }

    @Test
    void wrongSecretKeyFails() {
        String token = JwtUtil.createJwt(SECRET_KEY, 3600_000, Map.of("userId", 1L));
        assertThrows(Exception.class, () -> JwtUtil.parseJwt("another-secret-key-that-is-long-enough", token));
    }

    @Test
    void shortKeyIsPaddedTo32Bytes() {
        String shortKey = "short";
        String token = JwtUtil.createJwt(shortKey, 3600_000, Map.of("userId", 42L));
        Claims claims = JwtUtil.parseJwt(shortKey, token);
        assertEquals(42L, claims.get("userId", Long.class));
    }
}
