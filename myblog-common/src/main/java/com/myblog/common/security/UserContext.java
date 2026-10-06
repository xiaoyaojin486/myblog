package com.myblog.common.security;

/**
 * 当前登录用户上下文（ThreadLocal）
 * 由 JWT 过滤器在请求进入时写入、请求结束时清理
 */
public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void clear() {
        USER_ID.remove();
    }
}