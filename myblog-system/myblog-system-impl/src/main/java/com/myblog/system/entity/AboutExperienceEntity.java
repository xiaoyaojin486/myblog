package com.myblog.system.entity;

import lombok.Data;

/**
 * 经历实体（about_experience）
 */
@Data
public class AboutExperienceEntity {

    private Long id;

    private String title;

    private String organization;

    private String startDate;

    private String endDate;

    private String description;

    private Integer sort;
}