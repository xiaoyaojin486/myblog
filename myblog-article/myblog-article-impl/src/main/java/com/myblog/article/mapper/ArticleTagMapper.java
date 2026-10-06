package com.myblog.article.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文章-标签关联表 Mapper
 */
@Mapper
public interface ArticleTagMapper {

    /** 查询文章关联的标签 ID 列表 */
    @Select("SELECT tag_id FROM article_tag WHERE article_id = #{articleId}")
    List<Long> selectTagIdsByArticleId(@Param("articleId") Long articleId);

    /** 删除文章的全部标签关联 */
    @Delete("DELETE FROM article_tag WHERE article_id = #{articleId}")
    int deleteByArticleId(@Param("articleId") Long articleId);

    /** 批量插入标签关联 */
    @Insert("<script>INSERT INTO article_tag (article_id, tag_id) VALUES " +
            "<foreach collection='tagIds' item='tagId' separator=','>(#{articleId}, #{tagId})</foreach></script>")
    int batchInsert(@Param("articleId") Long articleId, @Param("tagIds") List<Long> tagIds);
}