package com.myblog.user.service;

import com.myblog.user.api.dto.LoginDTO;
import com.myblog.user.api.dto.PasswordUpdateDTO;
import com.myblog.user.api.dto.ProfileUpdateDTO;
import com.myblog.user.api.vo.LoginVO;
import com.myblog.user.api.vo.ProfileVO;

/**
 * 用户业务接口（模块内部使用）
 */
public interface UserService {

    /**
     * 登录：校验账号密码并签发 JWT
     *
     * @param dto 登录入参
     * @return token 与用户信息
     */
    LoginVO login(LoginDTO dto);

    /** 个人资料（单管理员场景，返回当前博主信息） */
    ProfileVO getProfile();

    /** 更新个人资料（用户名不可改） */
    ProfileVO updateProfile(ProfileUpdateDTO dto);

    /** 修改密码（修改成功后需重新登录） */
    void updatePassword(PasswordUpdateDTO dto);
}