package bank.testovoe.service.impl;

import bank.testovoe.exception.JwtAuthenticationException;
import bank.testovoe.model.User;
import bank.testovoe.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @PostConstruct
    public void init() {
        log.info("JWT Secret: {}", secret);
    }

    @Override
    public String generateToken(User user) {
        long JWT_TOKEN_VALIDITY = 24 * 60 * 60 * 1000;
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    @Override
    public boolean isTokenValid(String token, User user) {
        return extractUsername(token).equals(user.getEmail()) && !isTokenExpired(token);
    }

    private Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secret.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("Token expired", e);
        } catch (JwtException e) {
            throw new JwtAuthenticationException("Invalid token", e);
        }
    }

    private boolean isTokenExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }
}
