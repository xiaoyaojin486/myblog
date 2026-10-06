package com.myblog.system.service.impl;

import com.myblog.common.exception.BusinessException;
import com.myblog.system.api.vo.ConfigVO;
import com.myblog.system.convert.ConfigConvert;
import com.myblog.system.entity.ConfigEntity;
import com.myblog.system.mapper.ConfigMapper;
import com.myblog.system.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置业务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final ConfigMapper configMapper;
    private final ConfigConvert configConvert;

    @Override
    public Map<String, String> getAll() {
        Map<String, String> map = new LinkedHashMap<>();
        configMapper.selectAll().forEach(entity -> map.put(entity.getConfigKey(), entity.getConfigValue()));
        return map;
    }

    @Override
    public List<ConfigVO> listConfigs() {
        return configConvert.toVOList(configMapper.selectAll());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfigs(Map<String, String> configMap) {
        if (configMap == null || configMap.isEmpty()) {
            return;
        }
        configMap.forEach((key, value) -> {
            if (configMapper.updateValue(key, value) == 0) {
                throw new BusinessException("配置项不存在：" + key);
            }
        });
        log.info("更新系统配置：{} 项", configMap.size());
    }
}