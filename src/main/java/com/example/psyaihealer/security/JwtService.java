package com.example.psyaihealer.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.Arrays;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final String secret;
    private final long expirationSeconds;

    public JwtService(
            @Value("${app.jwt.secret:auto-generate}") String secret,
            @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds,
            Environment environment) {
        boolean isProd = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("prod") || p.equalsIgnoreCase("production"));
        boolean missingSecret = isMissingOrPlaceholder(secret);

        String effective = secret;
        if (!missingSecret) {
            // Validate early so login won't fail later with WeakKeyException.
            // If invalid/weak in non-prod, we auto-generate.
            try {
                byte[] keyBytes = decodeSecret(secret);
                if (keyBytes.length < 32) {
                    throw new IllegalArgumentException("JWT 密钥长度不足（<32字节）");
                }
            } catch (Exception ex) {
                missingSecret = true;
                log.warn("JWT 密钥无效或过弱：{}", ex.getMessage());
            }
        }

        if (missingSecret) {
            if (isProd) {
                throw new IllegalStateException("生产环境必须配置强 JWT 密钥：app.jwt.secret / APP_JWT_SECRET（>=32字节，或Base64/Base64URL编码的>=32字节密钥）");
            }
            effective = generateRandomSecret();
            log.warn("未配置/配置了弱JWT密钥（非生产环境），已为本次运行生成临时密钥；重启将失效。建议设置 APP_JWT_SECRET 以保证 token 稳定。");
        }

        this.secret = effective;
        this.expirationSeconds = expirationSeconds;
    }

    private static boolean isMissingOrPlaceholder(String secret) {
        if (secret == null) return true;
        String s = secret.trim();
        if (s.isEmpty()) return true;
        if ("auto-generate".equalsIgnoreCase(s)) return true;
        // Common placeholders used in sample configs.
        if (s.startsWith("CHANGE_ME")) return true;
        return false;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(String username, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = decodeSecret(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] decodeSecret(String value) {
        if (value == null) {
            throw new IllegalArgumentException("JWT 密钥不能为空");
        }

        // Support both Base64 and Base64URL (Base64URL commonly contains '-' and '_' characters).
        try {
            byte[] decoded = Decoders.BASE64.decode(value);
            if (decoded != null && decoded.length > 0) {
                return decoded;
            }
        } catch (DecodingException | IllegalArgumentException ignored) {
            // ignore
        }

        try {
            byte[] decoded = Decoders.BASE64URL.decode(value);
            if (decoded != null && decoded.length > 0) {
                return decoded;
            }
        } catch (DecodingException | IllegalArgumentException ignored) {
            // ignore
        }

        // Fallback: treat it as a raw secret string.
        // For HS256, require at least 32 bytes (256 bits).
        byte[] raw = value.getBytes(StandardCharsets.UTF_8);
        return raw;
    }

    private String generateRandomSecret() {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        return Encoders.BASE64.encode(key.getEncoded());
    }
}
