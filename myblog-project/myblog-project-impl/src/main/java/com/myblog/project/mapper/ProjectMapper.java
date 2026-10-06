package com.myblog.project.mapper;

import com.myblog.project.entity.ProjectEntity;
import com.myblog.project.query.ProjectQuery;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 项目表 Mapper（只负责数据库访问，必须明确字段，禁止 SELECT *）
 * 列表查询不加载 LONGTEXT 详情内容，只在详情查询中加载
 */
@Mapper
public interface ProjectMapper {

    /** 上架项目（列表字段，不含内容）；progress 非空时按进度筛选 */
    @Select("<script>" +
            "SELECT id, name, description, cover_image, tech_stack, github_url, demo_url, sort, progress, status, create_time, update_time " +
            "FROM project WHERE status = 1 " +
            "<if test=\"progress != null\">AND progress = #{progress} </if>" +
            " ORDER BY sort ASC, create_time DESC" +
            "</script>")
    List<ProjectEntity> selectOnline(@Param("progress") Integer progress);

    /** 条件分页查询（后台，列表字段） */
    @Select("<script>" +
            "SELECT id, name, description, cover_image, tech_stack, github_url, demo_url, sort, progress, status, create_time, update_time " +
            "FROM project " +
            "<where>" +
            "  <if test=\"q.status != null\">AND status = #{q.status} </if>" +
            "  <if test=\"q.progress != null\">AND progress = #{q.progress} </if>" +
            "  <if test=\"q.keyword != null and q.keyword != ''\">AND name LIKE CONCAT('%', #{q.keyword}, '%') </if>" +
            "</where>" +
            " ORDER BY sort ASC, create_time DESC" +
            "</script>")
    List<ProjectEntity> selectPageByQuery(@Param("q") ProjectQuery query);

    /** 详情查询（含内容） */
    @Select("SELECT id, name, description, content, cover_image, tech_stack, github_url, demo_url, sort, progress, status, create_time, update_time " +
            "FROM project WHERE id = #{id}")
    ProjectEntity selectDetailById(@Param("id") Long id);

    /** 新增，回填主键 */
    @Insert("INSERT INTO project (name, description, content, cover_image, tech_stack, github_url, demo_url, sort, progress, status, create_time, update_time) " +
            "VALUES (#{name}, #{description}, #{content}, #{coverImage}, #{techStack}, #{githubUrl}, #{demoUrl}, #{sort}, #{progress}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProjectEntity entity);

    /** 更新项目 */
    @Update("UPDATE project SET name = #{name}, description = #{description}, content = #{content}, cover_image = #{coverImage}, " +
            "tech_stack = #{techStack}, github_url = #{githubUrl}, demo_url = #{demoUrl}, sort = #{sort}, progress = #{progress}, status = #{status}, " +
            "update_time = NOW() WHERE id = #{id}")
    int update(ProjectEntity entity);

    /** 删除项目 */
    @Delete("DELETE FROM project WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}