package com.myblog.article.convert;

import com.myblog.article.api.dto.ArticleCreateDTO;
import com.myblog.article.api.dto.ArticleUpdateDTO;
import com.myblog.article.api.vo.ArticleListVO;
import com.myblog.article.api.vo.ArticleVO;
import com.myblog.article.entity.ArticleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 文章对象转换器（DTO/Entity/VO 转换统一走 Convert 层）
 */
@Mapper(componentModel = "spring")
public interface ArticleConvert {

    /** CreateDTO -> Entity */
    ArticleEntity toEntity(ArticleCreateDTO dto);

    /** UpdateDTO -> Entity（更新到已有实体，状态与统计字段不变） */
    void updateEntity(ArticleUpdateDTO dto, @MappingTarget ArticleEntity entity);

    /** Entity -> 详情 VO（分类名/标签由 Service 补全） */
    ArticleVO toVO(ArticleEntity entity);

    /** Entity 列表 -> 列表 VO 列表（分类名由 Service 补全） */
    List<ArticleListVO> toListVOList(List<ArticleEntity> list);
}