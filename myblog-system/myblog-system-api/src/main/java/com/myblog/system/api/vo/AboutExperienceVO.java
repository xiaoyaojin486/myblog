package com.myblog.system.api.vo;

import lombok.Data;

/**
 * 经历出参
 */
@Data
public class AboutExperienceVO {

    private Long id;

    /** 经历标题 */
    private String title;

    /** 组织/机构 */
    private String organization;

    /** 开始时间（如 2023-09） */
    private String startDate;

    /** 结束时间（空表示至今） */
    private String endDate;

    private String description;
}