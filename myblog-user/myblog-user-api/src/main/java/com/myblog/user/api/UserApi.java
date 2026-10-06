package com.myblog.user.api;

import com.myblog.user.api.vo.UserVO;

/**
 * 用户模块对外接口
 * 注意：跨模块调用只能注入本接口，禁止依赖 user-impl 的内部实现
 */
public interface UserApi {

    /**
     * 根据用户 ID 查询用户信息
     *
     * @param id 用户 ID
     * @return 用户信息（不存在时返回 null）
     */
    UserVO getById(Long id);
}