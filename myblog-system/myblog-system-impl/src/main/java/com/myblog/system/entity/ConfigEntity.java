package com.myblog.system.entity;

import lombok.Data;

/**
 * 系统配置表实体（sys_config）
 * 注意：Entity 只在 Service/Mapper 内部使用，不返回前端
 */
@Data
public class ConfigEntity {

    private Long id;

    /** 配置键（唯一） */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 备注 */
    private String remark;
}