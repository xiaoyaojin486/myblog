package com.myblog.system.api.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 关于我出参（自我介绍 + 简历 + 技能栈 + 经历）
 */
@Data
public class AboutVO {

    /** 自我介绍（Markdown） */
    private String content;

    /** 简历文件地址（空表示不提供下载） */
    private String resumeUrl;

    private List<AboutSkillVO> skills = new ArrayList<>();

    private List<AboutExperienceVO> experiences = new ArrayList<>();
}