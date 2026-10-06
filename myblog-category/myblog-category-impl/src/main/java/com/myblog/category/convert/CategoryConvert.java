package com.myblog.category.convert;

import com.myblog.category.api.dto.CategoryCreateDTO;
import com.myblog.category.api.dto.CategoryUpdateDTO;
import com.myblog.category.api.vo.CategoryVO;
import com.myblog.category.entity.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 分类对象转换器（DTO/Entity/VO 转换统一走 Convert 层）
 */
@Mapper(componentModel = "spring")
public interface CategoryConvert {

    /** CreateDTO -> Entity */
    CategoryEntity toEntity(CategoryCreateDTO dto);

    /** UpdateDTO -> Entity（更新到已有实体，id 不变） */
    void updateEntity(CategoryUpdateDTO dto, @MappingTarget CategoryEntity entity);

    /** Entity -> VO */
    CategoryVO toVO(CategoryEntity entity);

    /** Entity 列表 -> VO 列表 */
    List<CategoryVO> toVOList(List<CategoryEntity> list);
}