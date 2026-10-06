package com.myblog.system.api.vo;

import lombok.Data;

/**
 * 技能栈出参
 */
@Data
public class AboutSkillVO {

    private Long id;

    /** 技能名称 */
    private String name;

    /** 分类标签（如 后端/前端/运维） */
    private String levelLabel;

    /** 掌握程度（0-100） */
    private Integer percent;
}