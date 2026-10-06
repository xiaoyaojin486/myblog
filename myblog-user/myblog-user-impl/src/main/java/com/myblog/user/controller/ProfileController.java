package com.myblog.user.controller;

import com.myblog.common.result.Result;
import com.myblog.user.api.dto.PasswordUpdateDTO;
import com.myblog.user.api.dto.ProfileUpdateDTO;
import com.myblog.user.api.vo.ProfileVO;
import com.myblog.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人资料接口（后台管理 + 前台公开信息）
 * 注意：统一前缀 /api 由 context-path 提供，此处不重复写
 */
@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    /** 博主公开信息（前台关于页：头像/签名等） */
    @GetMapping("/profile")
    public Result<ProfileVO> publicProfile() {
        return Result.success(userService.getProfile());
    }

    /** 个人资料（后台） */
    @GetMapping("/admin/profile")
    public Result<ProfileVO> profile() {
        return Result.success(userService.getProfile());
    }

    /** 更新个人资料（后台） */
    @PutMapping("/admin/profile")
    public Result<ProfileVO> update(@RequestBody @Valid ProfileUpdateDTO dto) {
        return Result.success(userService.updateProfile(dto));
    }

    /** 修改密码（后台，修改成功后需重新登录） */
    @PutMapping("/admin/profile/password")
    public Result<Void> updatePassword(@RequestBody @Valid PasswordUpdateDTO dto) {
        userService.updatePassword(dto);
        return Result.success();
    }
}