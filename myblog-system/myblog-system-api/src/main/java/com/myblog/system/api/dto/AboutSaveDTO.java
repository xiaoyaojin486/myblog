package com.myblog.system.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 保存关于我入参（技能栈与经历为全量覆盖）
 */
@Data
public class AboutSaveDTO {

    /** 自我介绍（Markdown，可空） */
    private String content;

    @Size(max = 255, message = "简历文件地址长度不能超过255")
    private String resumeUrl;

    @Valid
    private List<AboutSkillDTO> skills;

    @Valid
    private List<AboutExperienceDTO> experiences;
}