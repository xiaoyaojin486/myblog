package com.myblog.category.service.impl;

import com.myblog.article.api.ArticleApi;
import com.myblog.category.api.CategoryApi;
import com.myblog.category.api.dto.CategoryCreateDTO;
import com.myblog.category.api.dto.CategoryUpdateDTO;
import com.myblog.category.api.vo.CategoryVO;
import com.myblog.category.convert.CategoryConvert;
import com.myblog.category.entity.CategoryEntity;
import com.myblog.category.mapper.CategoryMapper;
import com.myblog.category.service.CategoryService;
import com.myblog.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分类业务实现（同时实现本地 CategoryService 与对外 CategoryApi）
 * 跨模块调用：只注入 ArticleApi（删除保护统计文章数）；article-impl 反向依赖 category-api，
 * 因此这里用 @Lazy 延迟注入打破 Spring bean 循环依赖
 */
@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService, CategoryApi {

    private final CategoryMapper categoryMapper;
    private final CategoryConvert categoryConvert;
    private final ArticleApi articleApi;

    public CategoryServiceImpl(CategoryMapper categoryMapper, CategoryConvert categoryConvert,
                               @Lazy ArticleApi articleApi) {
        this.categoryMapper = categoryMapper;
        this.categoryConvert = categoryConvert;
        this.articleApi = articleApi;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CategoryCreateDTO dto) {
        if (categoryMapper.selectByName(dto.getName()) != null) {
            throw new BusinessException("分类名已存在");
        }
        CategoryEntity entity = categoryConvert.toEntity(dto);
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        categoryMapper.insert(entity);
        log.info("新增分类：{}（id={}）", entity.getName(), entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CategoryUpdateDTO dto) {
        CategoryEntity entity = categoryMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("分类不存在");
        }
        CategoryEntity sameName = categoryMapper.selectByName(dto.getName());
        if (sameName != null && !sameName.getId().equals(id)) {
            throw new BusinessException("分类名已存在");
        }
        categoryConvert.updateEntity(dto, entity);
        categoryMapper.update(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (categoryMapper.selectById(id) == null) {
            throw new BusinessException("分类不存在");
        }
        if (articleApi.countByCategoryId(id) > 0) {
            throw new BusinessException("该分类下有文章，不能删除");
        }
        categoryMapper.deleteById(id);
    }

    @Override
    public List<CategoryVO> listAll(String keyword) {
        return categoryConvert.toVOList(categoryMapper.selectAll(keyword));
    }

    /** CategoryApi：全部分类（供其他模块调用，不做关键字过滤） */
    @Override
    public List<CategoryVO> listAll() {
        return listAll(null);
    }

    @Override
    public CategoryVO getById(Long id) {
        return categoryConvert.toVO(categoryMapper.selectById(id));
    }
}