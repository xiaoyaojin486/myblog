package com.myblog.toolbox.convert;

import com.myblog.toolbox.api.dto.ToolCategoryDTO;
import com.myblog.toolbox.api.vo.ToolCategoryVO;
import com.myblog.toolbox.entity.ToolCategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 分类对象转换器（DTO/Entity/VO 转换统一走 Convert 层）
 */
@Mapper(componentModel = "spring")
public interface ToolCategoryConvert {

    /** DTO -> Entity */
    ToolCategoryEntity toEntity(ToolCategoryDTO dto);

    /** DTO -> Entity（更新到已有实体，id 不变） */
    void updateEntity(ToolCategoryDTO dto, @MappingTarget ToolCategoryEntity entity);

    /** Entity -> VO */
    ToolCategoryVO toVO(ToolCategoryEntity entity);

    /** Entity 列表 -> VO 列表 */
    List<ToolCategoryVO> toVOList(List<ToolCategoryEntity> list);
}
