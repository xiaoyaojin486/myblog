package com.myblog.system.convert;

import com.myblog.system.api.vo.ConfigVO;
import com.myblog.system.entity.ConfigEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 系统配置对象转换器（DTO/Entity/VO 转换统一走 Convert 层）
 */
@Mapper(componentModel = "spring")
public interface ConfigConvert {

    /** Entity -> VO */
    ConfigVO toVO(ConfigEntity entity);

    /** Entity 列表 -> VO 列表 */
    List<ConfigVO> toVOList(List<ConfigEntity> list);
}