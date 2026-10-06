package com.myblog.system.api.vo;

import lombok.Data;

/**
 * 系统配置出参（后台设置页，含备注作为表单标签）
 */
@Data
public class ConfigVO {

    private Long id;

    /** 配置键（如 site_name） */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 备注（如：站点名称） */
    private String remark;
}