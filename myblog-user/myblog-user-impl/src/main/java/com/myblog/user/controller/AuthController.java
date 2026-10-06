package com.myblog.user.controller;

import com.myblog.common.result.Result;
import com.myblog.user.api.dto.LoginDTO;
import com.myblog.user.api.vo.LoginVO;
import com.myblog.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 * 注意：统一前缀 /api 由 context-path 提供，此处不重复写
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /** 登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success(userService.login(dto));
    }
}