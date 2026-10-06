package com.myblog.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：Token 生成与解析
 */
@Component
public class JwtUtil {

    /** 签名密钥（HS256 要求长度 >= 32 字符，来自 .env / 环境变量 JWT_SECRET） */
    @Value("${jwt.secret}")
    private String secret;

    /** 过期时间（小时） */
    @Value("${jwt.expire-hours:24}")
    private long expireHours;

    /**
     * 生成 Token
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return JWT 字符串
     */
    public String createToken(Long userId, String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireHours * 60 * 60 * 1000);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey())
                .compact();
    }

    /**
     * 解析 Token（无效或过期会抛出 JwtException，由调用方处理）
     *
     * @param token JWT 字符串
     * @return 载荷
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}