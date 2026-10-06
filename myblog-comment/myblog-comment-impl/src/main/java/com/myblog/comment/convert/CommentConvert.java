package com.myblog.comment.convert;

import com.myblog.comment.api.dto.CommentCreateDTO;
import com.myblog.comment.api.vo.AdminCommentVO;
import com.myblog.comment.api.vo.CommentVO;
import com.myblog.comment.entity.CommentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 评论对象转换器（DTO/Entity/VO 转换统一走 Convert 层）
 */
@Mapper(componentModel = "spring")
public interface CommentConvert {

    /** CreateDTO -> Entity */
    CommentEntity toEntity(CommentCreateDTO dto);

    /** Entity -> 前台 VO（blogger：博主回复时 user_id 非空） */
    @Mapping(target = "blogger", expression = "java(entity.getUserId() != null)")
    CommentVO toVO(CommentEntity entity);

    /** Entity 列表 -> 前台 VO 列表 */
    List<CommentVO> toVOList(List<CommentEntity> list);

    /** Entity -> 后台 VO（blogger：博主回复时 user_id 非空） */
    @Mapping(target = "blogger", expression = "java(entity.getUserId() != null)")
    AdminCommentVO toAdminVO(CommentEntity entity);

    /** Entity 列表 -> 后台 VO 列表 */
    List<AdminCommentVO> toAdminVOList(List<CommentEntity> list);
}