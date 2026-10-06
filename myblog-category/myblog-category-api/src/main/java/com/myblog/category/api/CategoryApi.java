package com.myblog.category.api;

import com.myblog.category.api.vo.CategoryVO;

import java.util.List;

/**
 * 分类模块对外接口
 * 注意：跨模块调用只能注入本接口，禁止依赖 myblog-category-impl 的内部实现
 */
public interface CategoryApi {

    /**
     * 全部分类（按排序）
     *
     * @return 分类列表
     */
    List<CategoryVO> listAll();

    /**
     * 根据 ID 查询分类
     *
     * @param id 分类 ID
     * @return 分类信息（不存在时返回 null）
     */
    CategoryVO getById(Long id);
}