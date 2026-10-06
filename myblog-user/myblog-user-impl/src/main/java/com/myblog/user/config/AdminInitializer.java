package com.myblog.user.config;

import com.myblog.user.entity.UserEntity;
import com.myblog.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 初始管理员账号初始化：首次启动时若 admin 不存在则自动创建
 * 初始密码来自 .env / 环境变量 ADMIN_INIT_PASSWORD（默认 admin123）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private static final String ADMIN_USERNAME = "admin";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /** 初始密码 */
    @Value("${admin.init-password:admin123}")
    private String initPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (userMapper.selectByUsername(ADMIN_USERNAME) != null) {
            return;
        }
        UserEntity admin = new UserEntity();
        admin.setUsername(ADMIN_USERNAME);
        admin.setPassword(passwordEncoder.encode(initPassword));
        admin.setNickname("博主");
        admin.setStatus(1);
        userMapper.insert(admin);
        log.info("首次启动：已创建管理员账号 admin（初始密码来自 ADMIN_INIT_PASSWORD 配置）");
    }
}