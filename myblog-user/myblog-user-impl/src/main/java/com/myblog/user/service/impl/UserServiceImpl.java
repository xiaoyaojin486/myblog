package com.myblog.user.service.impl;

import com.myblog.common.exception.BusinessException;
import com.myblog.common.security.JwtUtil;
import com.myblog.user.api.UserApi;
import com.myblog.user.api.dto.LoginDTO;
import com.myblog.user.api.dto.PasswordUpdateDTO;
import com.myblog.user.api.dto.ProfileUpdateDTO;
import com.myblog.user.api.vo.LoginVO;
import com.myblog.user.api.vo.ProfileVO;
import com.myblog.user.api.vo.UserVO;
import com.myblog.user.convert.UserConvert;
import com.myblog.user.entity.UserEntity;
import com.myblog.user.mapper.UserMapper;
import com.myblog.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户业务实现（同时实现本地 UserService 与对外 UserApi）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserApi {

    private final UserMapper userMapper;
    private final UserConvert userConvert;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginVO login(LoginDTO dto) {
        UserEntity entity = userMapper.selectByUsername(dto.getUsername());
        if (entity == null || !passwordEncoder.matches(dto.getPassword(), entity.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (entity.getStatus() != null && entity.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        String token = jwtUtil.createToken(entity.getId(), entity.getUsername());
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(userConvert.toVO(entity));
        log.info("用户登录成功：{}", entity.getUsername());
        return vo;
    }

    @Override
    public ProfileVO getProfile() {
        return userConvert.toProfileVO(requireUser());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProfileVO updateProfile(ProfileUpdateDTO dto) {
        UserEntity entity = requireUser();
        userConvert.updateEntity(dto, entity);
        userMapper.updateProfile(entity);
        log.info("更新个人资料：{}", entity.getUsername());
        return userConvert.toProfileVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(PasswordUpdateDTO dto) {
        UserEntity entity = requireUser();
        if (!passwordEncoder.matches(dto.getOldPassword(), entity.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), entity.getPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }
        userMapper.updatePassword(entity.getId(), passwordEncoder.encode(dto.getNewPassword()));
        log.info("修改密码成功：{}", entity.getUsername());
    }

    @Override
    public UserVO getById(Long id) {
        return userConvert.toVO(userMapper.selectById(id));
    }

    // ==================== 私有方法 ====================

    /** 当前博主账号（单管理员博客，取首个用户） */
    private UserEntity requireUser() {
        UserEntity entity = userMapper.selectFirst();
        if (entity == null) {
            throw new BusinessException("用户不存在");
        }
        return entity;
    }
}