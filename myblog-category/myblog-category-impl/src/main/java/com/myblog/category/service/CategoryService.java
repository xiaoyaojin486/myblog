package com.myblog.category.service;

import com.myblog.category.api.dto.CategoryCreateDTO;
import com.myblog.category.api.dto.CategoryUpdateDTO;
import com.myblog.category.api.vo.CategoryVO;

import java.util.List;

/**
 * 分类业务接口（模块内部使用）
 */
public interface CategoryService {

    /**
     * 分类列表（keyword 为空时返回全部；非空时按名称模糊匹配）
     *
     * @param keyword 搜索关键字（可空）
     */
    List<CategoryVO> listAll(String keyword);

    /**
     * 新增分类
     *
     * @return 新分类 ID
     */
    Long create(CategoryCreateDTO dto);

    /**
     * 更新分类
     *
     * @param id  分类 ID（路径参数）
     * @param dto 更新内容
     */
    void update(Long id, CategoryUpdateDTO dto);

    /**
     * 删除分类
     */
    void delete(Long id);
}