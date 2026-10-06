package com.myblog.system.controller;

import com.myblog.common.result.Result;
import com.myblog.system.api.vo.ConfigVO;
import com.myblog.system.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 系统配置接口（前台读取 + 后台设置）
 * 注意：统一前缀 /api 由 context-path 提供，此处不重复写
 */
@RestController
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    /** 全部配置键值对（前台公共：站点名/描述/页脚/关于页等） */
    @GetMapping("/config/all")
    public Result<Map<String, String>> all() {
        return Result.success(configService.getAll());
    }

    /** 配置列表（后台设置页，含备注） */
    @GetMapping("/admin/config/list")
    public Result<List<ConfigVO>> list() {
        return Result.success(configService.listConfigs());
    }

    /** 批量更新配置（后台） */
    @PutMapping("/admin/config")
    public Result<Void> update(@RequestBody Map<String, String> configMap) {
        configService.updateConfigs(configMap);
        return Result.success();
    }
}