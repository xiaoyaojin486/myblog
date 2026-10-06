package com.myblog.system.service;

import com.myblog.system.api.vo.ConfigVO;

import java.util.List;
import java.util.Map;

/**
 * 系统配置业务接口（模块内部使用）
 */
public interface ConfigService {

    /** 全部配置（键值对，前台调用） */
    Map<String, String> getAll();

    /** 全部配置（列表，后台设置页调用，含备注） */
    List<ConfigVO> listConfigs();

    /** 批量更新配置（只更新已存在的键） */
    void updateConfigs(Map<String, String> configMap);
}