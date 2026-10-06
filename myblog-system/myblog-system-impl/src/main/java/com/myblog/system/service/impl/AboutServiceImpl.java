package com.myblog.system.service.impl;

import com.myblog.system.api.dto.AboutExperienceDTO;
import com.myblog.system.api.dto.AboutSaveDTO;
import com.myblog.system.api.dto.AboutSkillDTO;
import com.myblog.system.api.vo.AboutVO;
import com.myblog.system.convert.AboutConvert;
import com.myblog.system.entity.AboutExperienceEntity;
import com.myblog.system.entity.AboutSkillEntity;
import com.myblog.system.mapper.AboutMapper;
import com.myblog.system.mapper.ConfigMapper;
import com.myblog.system.service.AboutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 关于我业务实现
 * 自我介绍与简历地址存 sys_config，技能栈/经历存独立表（保存时全量覆盖）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AboutServiceImpl implements AboutService {

    private static final String KEY_CONTENT = "about_content";
    private static final String KEY_RESUME_URL = "about_resume_url";

    private final AboutMapper aboutMapper;
    private final AboutConvert aboutConvert;
    private final ConfigMapper configMapper;

    @Override
    public AboutVO getAbout() {
        AboutVO vo = new AboutVO();
        vo.setContent(configMapper.selectValueByKey(KEY_CONTENT));
        vo.setResumeUrl(configMapper.selectValueByKey(KEY_RESUME_URL));
        vo.setSkills(aboutConvert.toSkillVOList(aboutMapper.selectSkills()));
        vo.setExperiences(aboutConvert.toExperienceVOList(aboutMapper.selectExperiences()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAbout(AboutSaveDTO dto) {
        configMapper.updateValue(KEY_CONTENT, dto.getContent() == null ? "" : dto.getContent());
        configMapper.updateValue(KEY_RESUME_URL, dto.getResumeUrl() == null ? "" : dto.getResumeUrl());

        // 技能栈全量覆盖
        aboutMapper.deleteAllSkills();
        if (dto.getSkills() != null) {
            int sort = 0;
            for (AboutSkillDTO item : dto.getSkills()) {
                AboutSkillEntity entity = aboutConvert.toSkillEntity(item);
                entity.setSort(sort++);
                aboutMapper.insertSkill(entity);
            }
        }

        // 经历全量覆盖
        aboutMapper.deleteAllExperiences();
        if (dto.getExperiences() != null) {
            int sort = 0;
            for (AboutExperienceDTO item : dto.getExperiences()) {
                AboutExperienceEntity entity = aboutConvert.toExperienceEntity(item);
                entity.setSort(sort++);
                aboutMapper.insertExperience(entity);
            }
        }

        log.info("保存关于我：技能 {} 项，经历 {} 项",
                dto.getSkills() == null ? 0 : dto.getSkills().size(),
                dto.getExperiences() == null ? 0 : dto.getExperiences().size());
    }
}