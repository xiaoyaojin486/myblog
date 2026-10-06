package com.myblog.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblog.common.result.Result;
import com.myblog.common.result.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 校验过滤器：拦截后台接口（/admin/**），校验 Authorization 头并写入 UserContext
 * 前台接口（/article/**、/profile、/config/all 等）不受影响
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /**
     * 只拦后台接口
     * getServletPath() 不含 context-path（/api），故这里判断 /admin/ 即可
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath().startsWith("/admin/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith(TOKEN_PREFIX)) {
                writeUnauthorized(response);
                return;
            }
            Claims claims = jwtUtil.parseToken(header.substring(TOKEN_PREFIX.length()));
            UserContext.setUserId(Long.valueOf(claims.getSubject()));
            chain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException e) {
            // Token 无效 / 已过期 / 被篡改
            writeUnauthorized(response);
        } finally {
            // Tomcat 线程池复用，必须清理，否则会串用户
            UserContext.clear();
        }
    }

    /** Filter 中抛出的异常不会走 @RestControllerAdvice，需自行输出统一响应体 */
    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(ResultCode.UNAUTHORIZED)));
    }
}