package com.myblog.toolbox.convert;

import com.myblog.toolbox.api.dto.ToolCommandCreateDTO;
import com.myblog.toolbox.api.dto.ToolCommandUpdateDTO;
import com.myblog.toolbox.api.vo.ToolCommandVO;
import com.myblog.toolbox.entity.ToolCommandEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 命令对象转换器（DTO/Entity/VO 转换统一走 Convert 层）
 */
@Mapper(componentModel = "spring")
public interface ToolCommandConvert {

    /** CreateDTO -> Entity */
    ToolCommandEntity toEntity(ToolCommandCreateDTO dto);

    /** UpdateDTO -> Entity（更新到已有实体，id 不变） */
    void updateEntity(ToolCommandUpdateDTO dto, @MappingTarget ToolCommandEntity entity);

    /** Entity -> VO */
    ToolCommandVO toVO(ToolCommandEntity entity);

    /** Entity 列表 -> VO 列表 */
    List<ToolCommandVO> toVOList(List<ToolCommandEntity> list);
}
