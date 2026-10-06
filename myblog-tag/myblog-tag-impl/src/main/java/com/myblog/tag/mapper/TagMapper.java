package com.myblog.tag.mapper;

import com.myblog.tag.entity.TagEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 标签表 Mapper（只负责数据库访问，必须明确字段，禁止 SELECT *）
 */
@Mapper
public interface TagMapper {

    /** 标签列表（按创建时间倒序；keyword 非空时按名称模糊过滤） */
    @Select("<script>" +
            "SELECT id, name, create_time FROM tag " +
            "<where>" +
            "  <if test=\"keyword != null and keyword != ''\">AND name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "</where>" +
            " ORDER BY create_time DESC" +
            "</script>")
    List<TagEntity> selectAll(@Param("keyword") String keyword);

    /** 按 ID 查询 */
    @Select("SELECT id, name, create_time FROM tag WHERE id = #{id}")
    TagEntity selectById(@Param("id") Long id);

    /** 按名称查询（用于重名校验） */
    @Select("SELECT id, name, create_time FROM tag WHERE name = #{name}")
    TagEntity selectByName(@Param("name") String name);

    /** 批量按 ID 查询 */
    @Select("<script>SELECT id, name, create_time FROM tag WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<TagEntity> selectByIds(@Param("ids") List<Long> ids);

    /** 新增，回填主键 */
    @Insert("INSERT INTO tag (name, create_time) VALUES (#{name}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TagEntity entity);

    /** 更新标签名 */
    @Update("UPDATE tag SET name = #{name} WHERE id = #{id}")
    int update(TagEntity entity);

    /** 按 ID 删除 */
    @Delete("DELETE FROM tag WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}