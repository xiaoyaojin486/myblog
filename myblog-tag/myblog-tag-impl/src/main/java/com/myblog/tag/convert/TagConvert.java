package com.myblog.tag.convert;

import com.myblog.tag.api.dto.TagCreateDTO;
import com.myblog.tag.api.vo.TagVO;
import com.myblog.tag.entity.TagEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 标签对象转换器（DTO/Entity/VO 转换统一走 Convert 层）
 */
@Mapper(componentModel = "spring")
public interface TagConvert {

    /** CreateDTO -> Entity */
    TagEntity toEntity(TagCreateDTO dto);

    /** Entity -> VO */
    TagVO toVO(TagEntity entity);

    /** Entity 列表 -> VO 列表 */
    List<TagVO> toVOList(List<TagEntity> list);
}