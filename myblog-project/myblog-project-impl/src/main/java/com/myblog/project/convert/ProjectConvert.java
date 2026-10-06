package com.myblog.project.convert;

import com.myblog.project.api.dto.ProjectCreateDTO;
import com.myblog.project.api.dto.ProjectUpdateDTO;
import com.myblog.project.api.vo.ProjectVO;
import com.myblog.project.entity.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 项目对象转换器（DTO/Entity/VO 转换统一走 Convert 层）
 */
@Mapper(componentModel = "spring")
public interface ProjectConvert {

    /** CreateDTO -> Entity */
    ProjectEntity toEntity(ProjectCreateDTO dto);

    /** UpdateDTO -> Entity（更新到已有实体，id 不变） */
    void updateEntity(ProjectUpdateDTO dto, @MappingTarget ProjectEntity entity);

    /** Entity -> VO */
    ProjectVO toVO(ProjectEntity entity);

    /** Entity 列表 -> VO 列表 */
    List<ProjectVO> toVOList(List<ProjectEntity> list);
}