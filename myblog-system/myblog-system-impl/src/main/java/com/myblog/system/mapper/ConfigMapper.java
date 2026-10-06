package com.myblog.system.mapper;

import com.myblog.system.entity.ConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 系统配置表 Mapper（只负责数据库访问，必须明确字段，禁止 SELECT *）
 */
@Mapper
public interface ConfigMapper {

    /** 全部配置（按固定 id 顺序，便于前台/后台稳定展示） */
    @Select("SELECT id, config_key, config_value, remark FROM sys_config ORDER BY id ASC")
    List<ConfigEntity> selectAll();

    /** 按配置键更新值（键不存在时返回 0） */
    @Update("UPDATE sys_config SET config_value = #{configValue} WHERE config_key = #{configKey}")
    int updateValue(@Param("configKey") String configKey, @Param("configValue") String configValue);

    /** 按配置键查询配置值（不存在返回 null） */
    @Select("SELECT config_value FROM sys_config WHERE config_key = #{configKey}")
    String selectValueByKey(@Param("configKey") String configKey);
}