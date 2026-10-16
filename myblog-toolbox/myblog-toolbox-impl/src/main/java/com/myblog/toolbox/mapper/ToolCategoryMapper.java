package com.myblog.toolbox.mapper;

import com.myblog.toolbox.entity.ToolCategoryEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 工具箱分类表 Mapper（只负责数据库访问，必须明确字段，禁止 SELECT *）
 */
@Mapper
public interface ToolCategoryMapper {

    /** 启用的分类（前台用），按排序 */
    @Select("SELECT id, name, sort, status, create_time, update_time FROM tool_category " +
            "WHERE status = 1 ORDER BY sort ASC, id ASC")
    List<ToolCategoryEntity> selectEnabled();

    /** 全部分类（后台用），按排序 */
    @Select("SELECT id, name, sort, status, create_time, update_time FROM tool_category " +
            "ORDER BY sort ASC, id ASC")
    List<ToolCategoryEntity> selectAll();

    @Select("SELECT id, name, sort, status, create_time, update_time FROM tool_category WHERE id = #{id}")
    ToolCategoryEntity selectById(@Param("id") Long id);

    /** 新增，回填主键 */
    @Insert("INSERT INTO tool_category (name, sort, status, create_time, update_time) " +
            "VALUES (#{name}, #{sort}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ToolCategoryEntity entity);

    @Update("UPDATE tool_category SET name = #{name}, sort = #{sort}, status = #{status}, update_time = NOW() WHERE id = #{id}")
    int update(ToolCategoryEntity entity);

    @Delete("DELETE FROM tool_category WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
