package com.myblog.comment.mapper;

import com.myblog.comment.entity.CommentCountEntity;
import com.myblog.comment.entity.CommentEntity;
import com.myblog.comment.query.CommentQuery;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 评论表 Mapper（只负责数据库访问，必须明确字段，禁止 SELECT *）
 */
@Mapper
public interface CommentMapper {

    /** 某篇文章已审核通过的评论（前台展示，按时间正序） */
    @Select("SELECT id, article_id, nickname, email, content, parent_id, user_id, status, ip, create_time " +
            "FROM comment WHERE article_id = #{articleId} AND status = 1 ORDER BY create_time ASC")
    List<CommentEntity> selectApprovedByArticleId(@Param("articleId") Long articleId);

    /** 条件分页查询（后台，待审核的排在最前，其余按时间倒序） */
    @Select("<script>" +
            "SELECT id, article_id, nickname, email, content, parent_id, user_id, status, ip, create_time " +
            "FROM comment " +
            "<where>" +
            "  <if test=\"q.articleId != null\">AND article_id = #{q.articleId} </if>" +
            "  <if test=\"q.status != null\">AND status = #{q.status} </if>" +
            "  <if test=\"q.keyword != null and q.keyword != ''\">" +
            "    AND (nickname LIKE CONCAT('%', #{q.keyword}, '%') " +
            "         OR content LIKE CONCAT('%', #{q.keyword}, '%') " +
            "         OR email LIKE CONCAT('%', #{q.keyword}, '%')) " +
            "  </if>" +
            "</where>" +
            " ORDER BY status ASC, create_time DESC" +
            "</script>")
    List<CommentEntity> selectPageByQuery(@Param("q") CommentQuery query);

    /** 根据 ID 查询 */
    @Select("SELECT id, article_id, nickname, email, content, parent_id, user_id, status, ip, create_time " +
            "FROM comment WHERE id = #{id}")
    CommentEntity selectById(@Param("id") Long id);

    /** 新增评论，回填主键 */
    @Insert("INSERT INTO comment (article_id, nickname, email, content, parent_id, user_id, status, ip, create_time) " +
            "VALUES (#{articleId}, #{nickname}, #{email}, #{content}, #{parentId}, #{userId}, #{status}, #{ip}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CommentEntity entity);

    /** 更新审核状态 */
    @Update("UPDATE comment SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 批量更新审核状态（后台批量通过 / 批量拒绝） */
    @Update("<script>" +
            "UPDATE comment SET status = #{status} WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int updateStatusBatch(@Param("ids") List<Long> ids, @Param("status") Integer status);

    /** 删除评论（连同其直接回复一起删除，避免留下悬空回复） */
    @Delete("DELETE FROM comment WHERE id = #{id} OR parent_id = #{id}")
    int deleteById(@Param("id") Long id);

    /** 批量删除（连同各自的直接回复） */
    @Delete("<script>" +
            "DELETE FROM comment WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            " OR parent_id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int deleteByIds(@Param("ids") List<Long> ids);

    /** 批量统计各文章的已通过评论数（文章列表展示用） */
    @Select("<script>" +
            "SELECT article_id AS articleId, COUNT(*) AS total FROM comment " +
            "WHERE status = 1 AND article_id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            " GROUP BY article_id" +
            "</script>")
    List<CommentCountEntity> countApprovedByArticleIds(@Param("ids") List<Long> ids);
}