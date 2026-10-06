package com.myblog.system.entity;

import lombok.Data;

/**
 * 技能栈实体（about_skill）
 */
@Data
public class AboutSkillEntity {

    private Long id;

    private String name;

    /** 分类标签（如 后端/前端/运维） */
    private String levelLabel;

    /** 掌握程度（0-100） */
    private Integer percent;

    private Integer sort;
}