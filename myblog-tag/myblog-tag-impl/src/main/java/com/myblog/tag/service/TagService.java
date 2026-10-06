package com.myblog.tag.service;

import com.myblog.tag.api.dto.TagCreateDTO;
import com.myblog.tag.api.dto.TagUpdateDTO;
import com.myblog.tag.api.vo.TagVO;

import java.util.List;

/**
 * 标签业务接口（模块内部使用）
 */
public interface TagService {

    /**
     * 标签列表（keyword 为空时返回全部；非空时按名称模糊匹配）
     *
     * @param keyword 搜索关键字（可空）
     */
    List<TagVO> listAll(String keyword);

    /**
     * 新增标签
     *
     * @return 新标签 ID
     */
    Long create(TagCreateDTO dto);

    /**
     * 更新标签名
     *
     * @param id  标签 ID（路径参数）
     * @param dto 更新内容
     */
    void update(Long id, TagUpdateDTO dto);

    /**
     * 删除标签（已被文章使用时不可删除）
     */
    void delete(Long id);
}