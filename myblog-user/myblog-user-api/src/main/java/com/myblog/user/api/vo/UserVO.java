package com.myblog.user.api.vo;

import lombok.Data;

/**
 * 用户信息出参（不包含密码等敏感字段）
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String email;
}