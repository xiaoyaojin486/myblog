package com.myblog.article.mapper;

import com.myblog.article.entity.ArticleEntity;
import com.myblog.article.query.ArticleQuery;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 文章表 Mapper（只负责数据库访问，必须明确字段，禁止 SELECT *）
 * 列表查询不加载 LONGTEXT 内容字段，只在详情查询中加载
 */
@Mapper
public interface ArticleMapper {

    /** 条件分页查询（列表字段，不含内容） */
    @Select("<script>" +
            "SELECT id, title, summary, cover_image, category_id, view_count, like_count, word_count, status, create_time, update_time " +
            "FROM article " +
            "<where>" +
            "  <if test=\"q.status != null\">AND status = #{q.status} </if>" +
            "  <if test=\"q.keyword != null and q.keyword != ''\">" +
            "    AND (title LIKE CONCAT('%', #{q.keyword}, '%') OR summary LIKE CONCAT('%', #{q.keyword}, '%')) " +
            "  </if>" +
            "  <if test=\"q.categoryId != null\">AND category_id = #{q.categoryId} </if>" +
            "  <if test=\"q.tagId != null\">AND id IN (SELECT article_id FROM article_tag WHERE tag_id = #{q.tagId}) </if>" +
            "</where>" +
            " ORDER BY create_time DESC" +
            "</script>")
    List<ArticleEntity> selectPageByQuery(@Param("q") ArticleQuery query);

    /** 详情查询（含内容） */
    @Select("SELECT id, title, content, summary, cover_image, category_id, view_count, like_count, word_count, status, create_time, update_time " +
            "FROM article WHERE id = #{id}")
    ArticleEntity selectDetailById(@Param("id") Long id);

    /** 最新已发布文章 */
    @Select("SELECT id, title, summary, cover_image, category_id, view_count, like_count, word_count, status, create_time, update_time " +
            "FROM article WHERE status = 1 ORDER BY create_time DESC LIMIT #{limit}")
    List<ArticleEntity> selectLatest(@Param("limit") int limit);

    /** 热门已发布文章（MVP：按浏览量、点赞数排序；周榜待 Redis 阶段实现） */
    @Select("SELECT id, title, summary, cover_image, category_id, view_count, like_count, word_count, status, create_time, update_time " +
            "FROM article WHERE status = 1 ORDER BY view_count DESC, like_count DESC LIMIT #{limit}")
    List<ArticleEntity> selectHot(@Param("limit") int limit);

    /** 新增文章，回填主键（初始为草稿） */
    @Insert("INSERT INTO article (title, content, summary, cover_image, category_id, view_count, like_count, word_count, status, create_time, update_time) " +
            "VALUES (#{title}, #{content}, #{summary}, #{coverImage}, #{categoryId}, 0, 0, #{wordCount}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ArticleEntity entity);

    /** 更新文章（不改变状态与统计数据） */
    @Update("UPDATE article SET title = #{title}, content = #{content}, summary = #{summary}, cover_image = #{coverImage}, " +
            "category_id = #{categoryId}, word_count = #{wordCount}, update_time = NOW() WHERE id = #{id}")
    int update(ArticleEntity entity);

    /** 发布文章 */
    @Update("UPDATE article SET status = 1, update_time = NOW() WHERE id = #{id}")
    int publish(@Param("id") Long id);

    /** 下线文章（回到草稿） */
    @Update("UPDATE article SET status = 0, update_time = NOW() WHERE id = #{id}")
    int unpublish(@Param("id") Long id);

    /** 删除文章 */
    @Delete("DELETE FROM article WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /** 浏览量 +1（MVP 直接更新，Redis 计数阶段改为批量回写） */
    @Update("UPDATE article SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    /** 已发布文章的点赞数（不存在或未发布返回 null） */
    @Select("SELECT like_count FROM article WHERE id = #{id} AND status = 1")
    Integer selectPublishedLikeCount(@Param("id") Long id);

    /** 点赞数 +1 */
    @Update("UPDATE article SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id);

    /** 按 ID 集合查询已发布文章（列表字段，周榜回填用；顺序由调用方按 Redis 排名重排） */
    @Select("<script>" +
            "SELECT id, title, summary, cover_image, category_id, view_count, like_count, word_count, status, create_time, update_time " +
            "FROM article WHERE status = 1 AND id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<ArticleEntity> selectPublishedByIds(@Param("ids") List<Long> ids);

    /** 分类下的文章数（含草稿，用于分类删除保护） */
    @Select("SELECT COUNT(*) FROM article WHERE category_id = #{categoryId}")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    /** 标签下的文章数（含草稿，用于标签删除保护） */
    @Select("SELECT COUNT(*) FROM article_tag WHERE tag_id = #{tagId}")
    long countByTagId(@Param("tagId") Long tagId);
}