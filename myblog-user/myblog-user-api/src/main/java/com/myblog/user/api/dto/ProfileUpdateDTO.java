package com.myblog.user.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新个人资料入参（用户名不可改）
 */
@Data
public class ProfileUpdateDTO {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    @Size(max = 255, message = "头像URL长度不能超过255")
    private String avatar;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    @Size(max = 200, message = "个人签名长度不能超过200")
    private String signature;

    @Size(max = 255, message = "GitHub 地址长度不能超过255")
    private String github;

    @Size(max = 255, message = "Gitee 地址长度不能超过255")
    private String gitee;

    @Size(max = 255, message = "掘金地址长度不能超过255")
    private String juejin;

    @Size(max = 255, message = "CSDN 地址长度不能超过255")
    private String csdn;

    @Size(max = 255, message = "微信二维码图片URL长度不能超过255")
    private String wechatQr;
}