package com.myblog.tag.api;

import com.myblog.tag.api.vo.TagVO;

import java.util.List;

/**
 * 标签模块对外接口
 * 注意：跨模块调用只能注入本接口，禁止依赖 myblog-tag-impl 的内部实现
 */
public interface TagApi {

    /**
     * 全部标签
     *
     * @return 标签列表
     */
    List<TagVO> listAll();

    /**
     * 根据 ID 查询标签
     *
     * @param id 标签 ID
     * @return 标签信息（不存在时返回 null）
     */
    TagVO getById(Long id);

    /**
     * 批量查询标签
     *
     * @param ids 标签 ID 集合（为空返回空列表）
     * @return 标签列表
     */
    List<TagVO> listByIds(List<Long> ids);
}