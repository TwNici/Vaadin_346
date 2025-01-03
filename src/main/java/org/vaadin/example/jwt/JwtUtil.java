package org.vaadin.example.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.util.Date;

public class JwtUtil {
    private static final String SECRET = "your-secret-key";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);
    private static final long EXPIRATION_TIME = 86400000L; // 1 day in milliseconds

    public static String generateToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(ALGORITHM);
    }

    public static String validateToken(String token) {
        try {
            return JWT.require(ALGORITHM)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Invalid token");
        }
    }
}