package com.myblog.user.api.vo;

import lombok.Data;

/**
 * 登录出参
 */
@Data
public class LoginVO {

    /** JWT 令牌 */
    private String token;

    /** 用户信息 */
    private UserVO user;
}