package com.myblog.tag.service.impl;

import com.myblog.article.api.ArticleApi;
import com.myblog.common.exception.BusinessException;
import com.myblog.tag.api.TagApi;
import com.myblog.tag.api.dto.TagCreateDTO;
import com.myblog.tag.api.dto.TagUpdateDTO;
import com.myblog.tag.api.vo.TagVO;
import com.myblog.tag.convert.TagConvert;
import com.myblog.tag.entity.TagEntity;
import com.myblog.tag.mapper.TagMapper;
import com.myblog.tag.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 标签业务实现（同时实现本地 TagService 与对外 TagApi）
 * 跨模块调用：只注入 ArticleApi（删除保护统计文章数）；article-impl 反向依赖 tag-api，
 * 因此这里用 @Lazy 延迟注入打破 Spring bean 循环依赖
 */
@Slf4j
@Service
public class TagServiceImpl implements TagService, TagApi {

    private final TagMapper tagMapper;
    private final TagConvert tagConvert;
    private final ArticleApi articleApi;

    public TagServiceImpl(TagMapper tagMapper, TagConvert tagConvert, @Lazy ArticleApi articleApi) {
        this.tagMapper = tagMapper;
        this.tagConvert = tagConvert;
        this.articleApi = articleApi;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TagCreateDTO dto) {
        if (tagMapper.selectByName(dto.getName()) != null) {
            throw new BusinessException("标签名已存在");
        }
        TagEntity entity = tagConvert.toEntity(dto);
        tagMapper.insert(entity);
        log.info("新增标签：{}（id={}）", entity.getName(), entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, TagUpdateDTO dto) {
        TagEntity entity = tagMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("标签不存在");
        }
        TagEntity sameName = tagMapper.selectByName(dto.getName());
        if (sameName != null && !sameName.getId().equals(id)) {
            throw new BusinessException("标签名已存在");
        }
        entity.setName(dto.getName());
        tagMapper.update(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (tagMapper.selectById(id) == null) {
            throw new BusinessException("标签不存在");
        }
        if (articleApi.countByTagId(id) > 0) {
            throw new BusinessException("该标签已被文章使用，不能删除");
        }
        tagMapper.deleteById(id);
    }

    @Override
    public List<TagVO> listAll(String keyword) {
        return tagConvert.toVOList(tagMapper.selectAll(keyword));
    }

    /** TagApi：全部标签（供其他模块调用，不做关键字过滤） */
    @Override
    public List<TagVO> listAll() {
        return listAll(null);
    }

    @Override
    public TagVO getById(Long id) {
        return tagConvert.toVO(tagMapper.selectById(id));
    }

    @Override
    public List<TagVO> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return tagConvert.toVOList(tagMapper.selectByIds(ids));
    }
}