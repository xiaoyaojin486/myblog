package com.myblog.user.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表实体（sys_user）
 * 注意：Entity 只在 Service/Mapper 内部使用，不返回前端
 */
@Data
public class UserEntity {

    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    private String email;

    /** 个人签名 */
    private String signature;

    /** GitHub 地址 */
    private String github;

    /** Gitee 地址 */
    private String gitee;

    /** 掘金地址 */
    private String juejin;

    /** CSDN 地址 */
    private String csdn;

    /** 微信二维码图片URL */
    private String wechatQr;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}