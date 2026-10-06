package com.myblog.common.result;

import lombok.Getter;

/**
 * 统一状态码
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "成功"),
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    ERROR(500, "系统异常");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}