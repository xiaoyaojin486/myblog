package com.myblog.system.convert;

import com.myblog.system.api.dto.AboutExperienceDTO;
import com.myblog.system.api.dto.AboutSkillDTO;
import com.myblog.system.api.vo.AboutExperienceVO;
import com.myblog.system.api.vo.AboutSkillVO;
import com.myblog.system.entity.AboutExperienceEntity;
import com.myblog.system.entity.AboutSkillEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 关于我对象转换器（DTO/Entity/VO 转换统一走 Convert 层）
 */
@Mapper(componentModel = "spring")
public interface AboutConvert {

    AboutSkillVO toSkillVO(AboutSkillEntity entity);

    List<AboutSkillVO> toSkillVOList(List<AboutSkillEntity> list);

    AboutSkillEntity toSkillEntity(AboutSkillDTO dto);

    AboutExperienceVO toExperienceVO(AboutExperienceEntity entity);

    List<AboutExperienceVO> toExperienceVOList(List<AboutExperienceEntity> list);

    AboutExperienceEntity toExperienceEntity(AboutExperienceDTO dto);
}