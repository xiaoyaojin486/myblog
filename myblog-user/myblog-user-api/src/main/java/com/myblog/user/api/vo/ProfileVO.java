package com.myblog.user.api.vo;

import lombok.Data;

/**
 * 个人资料出参（不含密码等敏感字段）
 */
@Data
public class ProfileVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String email;

    /** 个人签名 */
    private String signature;

    private String github;

    private String gitee;

    private String juejin;

    private String csdn;

    /** 微信二维码图片URL */
    private String wechatQr;
}