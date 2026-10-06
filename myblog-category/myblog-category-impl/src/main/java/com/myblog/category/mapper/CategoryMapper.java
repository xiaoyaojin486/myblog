package com.myblog.category.mapper;

import com.myblog.category.entity.CategoryEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 分类表 Mapper（只负责数据库访问，必须明确字段，禁止 SELECT *）
 */
@Mapper
public interface CategoryMapper {

    /** 分类列表（按排序升序；keyword 非空时按名称模糊过滤） */
    @Select("<script>" +
            "SELECT id, name, sort, create_time, update_time FROM category " +
            "<where>" +
            "  <if test=\"keyword != null and keyword != ''\">AND name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "</where>" +
            " ORDER BY sort ASC, create_time DESC" +
            "</script>")
    List<CategoryEntity> selectAll(@Param("keyword") String keyword);

    /** 按 ID 查询 */
    @Select("SELECT id, name, sort, create_time, update_time FROM category WHERE id = #{id}")
    CategoryEntity selectById(@Param("id") Long id);

    /** 按名称查询（用于重名校验） */
    @Select("SELECT id, name, sort, create_time, update_time FROM category WHERE name = #{name}")
    CategoryEntity selectByName(@Param("name") String name);

    /** 新增，回填主键 */
    @Insert("INSERT INTO category (name, sort, create_time, update_time) VALUES (#{name}, #{sort}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CategoryEntity entity);

    /** 更新名称与排序 */
    @Update("UPDATE category SET name = #{name}, sort = #{sort}, update_time = NOW() WHERE id = #{id}")
    int update(CategoryEntity entity);

    /** 按 ID 删除 */
    @Delete("DELETE FROM category WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}