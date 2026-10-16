package com.myblog.toolbox.mapper;

import com.myblog.toolbox.entity.ToolCommandEntity;
import com.myblog.toolbox.query.ToolCommandQuery;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 工具箱命令表 Mapper（只负责数据库访问，必须明确字段，禁止 SELECT *）
 * 查询联表 tool_category 带出分类名 category_name
 */
@Mapper
public interface ToolCommandMapper {

    /** 上架命令（前台用，联表带出分类名），按分类排序 + 命令排序 */
    @Select("SELECT t.id, t.name, t.command, t.category_id, c.name AS category_name, t.description, " +
            "t.risk_level, t.sort, t.status, t.create_time, t.update_time " +
            "FROM tool_command t LEFT JOIN tool_category c ON t.category_id = c.id " +
            "WHERE t.status = 1 " +
            "ORDER BY c.sort ASC, t.sort ASC, t.id ASC")
    List<ToolCommandEntity> selectOnline();

    /** 条件分页查询（后台，联表带出分类名） */
    @Select("<script>" +
            "SELECT t.id, t.name, t.command, t.category_id, c.name AS category_name, t.description, " +
            "t.risk_level, t.sort, t.status, t.create_time, t.update_time " +
            "FROM tool_command t LEFT JOIN tool_category c ON t.category_id = c.id " +
            "<where>" +
            "  <if test=\"q.categoryId != null\">AND t.category_id = #{q.categoryId} </if>" +
            "  <if test=\"q.riskLevel != null\">AND t.risk_level = #{q.riskLevel} </if>" +
            "  <if test=\"q.status != null\">AND t.status = #{q.status} </if>" +
            "  <if test=\"q.keyword != null and q.keyword != ''\">" +
            "    AND (t.name LIKE CONCAT('%', #{q.keyword}, '%') OR t.command LIKE CONCAT('%', #{q.keyword}, '%') " +
            "         OR t.description LIKE CONCAT('%', #{q.keyword}, '%')) </if>" +
            "</where>" +
            " ORDER BY t.sort ASC, t.id ASC" +
            "</script>")
    List<ToolCommandEntity> selectPageByQuery(@Param("q") ToolCommandQuery query);

    /** 单条查询（联表带出分类名） */
    @Select("SELECT t.id, t.name, t.command, t.category_id, c.name AS category_name, t.description, " +
            "t.risk_level, t.sort, t.status, t.create_time, t.update_time " +
            "FROM tool_command t LEFT JOIN tool_category c ON t.category_id = c.id WHERE t.id = #{id}")
    ToolCommandEntity selectById(@Param("id") Long id);

    /** 新增，回填主键 */
    @Insert("INSERT INTO tool_command (name, command, category_id, description, risk_level, sort, status, create_time, update_time) " +
            "VALUES (#{name}, #{command}, #{categoryId}, #{description}, #{riskLevel}, #{sort}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ToolCommandEntity entity);

    @Update("UPDATE tool_command SET name = #{name}, command = #{command}, category_id = #{categoryId}, description = #{description}, " +
            "risk_level = #{riskLevel}, sort = #{sort}, status = #{status}, update_time = NOW() WHERE id = #{id}")
    int update(ToolCommandEntity entity);

    @Delete("DELETE FROM tool_command WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /** 某分类下的命令数（删除分类前校验） */
    @Select("SELECT COUNT(*) FROM tool_command WHERE category_id = #{categoryId}")
    long countByCategory(@Param("categoryId") Long categoryId);
}
