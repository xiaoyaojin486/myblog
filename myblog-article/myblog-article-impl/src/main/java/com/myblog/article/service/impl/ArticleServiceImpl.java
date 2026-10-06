package com.myblog.article.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.myblog.article.api.ArticleApi;
import com.myblog.article.api.dto.ArticleCreateDTO;
import com.myblog.article.api.dto.ArticleUpdateDTO;
import com.myblog.article.api.vo.ArticleListVO;
import com.myblog.article.api.vo.ArticleVO;
import com.myblog.article.api.vo.LikeVO;
import com.myblog.article.convert.ArticleConvert;
import com.myblog.article.entity.ArticleEntity;
import com.myblog.article.mapper.ArticleMapper;
import com.myblog.article.mapper.ArticleTagMapper;
import com.myblog.article.query.ArticleQuery;
import com.myblog.article.service.ArticleService;
import com.myblog.category.api.CategoryApi;
import com.myblog.category.api.vo.CategoryVO;
import com.myblog.comment.api.CommentApi;
import com.myblog.common.exception.BusinessException;
import com.myblog.common.result.PageResult;
import com.myblog.tag.api.TagApi;
import com.myblog.tag.api.vo.TagVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文章业务实现（同时实现本地 ArticleService 与对外 ArticleApi）
 * 跨模块调用：只注入 CategoryApi / TagApi，禁止访问其他模块数据库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService, ArticleApi {

    /** Redis 键：文章点赞去重集合（成员=客户端标识，TTL 24 小时） */
    private static final String LIKE_KEY_PREFIX = "article:like:";

    /** Redis 键：周热度 ZSET 前缀（成员=文章ID，分值=热度） */
    private static final String HOT_WEEK_KEY_PREFIX = "article:hot:week:";

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleConvert articleConvert;
    private final CategoryApi categoryApi;
    private final TagApi tagApi;
    private final CommentApi commentApi;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ArticleCreateDTO dto) {
        checkCategory(dto.getCategoryId());
        ArticleEntity entity = articleConvert.toEntity(dto);
        entity.setStatus(0); // 新建为草稿
        entity.setWordCount(countWords(dto.getContent()));
        articleMapper.insert(entity);
        saveTags(entity.getId(), dto.getTagIds());
        log.info("新增文章：{}（id={}）", entity.getTitle(), entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ArticleUpdateDTO dto) {
        ArticleEntity entity = requireArticle(id);
        checkCategory(dto.getCategoryId());
        articleConvert.updateEntity(dto, entity);
        entity.setWordCount(countWords(dto.getContent()));
        articleMapper.update(entity);
        // 标签全量覆盖
        articleTagMapper.deleteByArticleId(id);
        saveTags(id, dto.getTagIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireArticle(id);
        articleTagMapper.deleteByArticleId(id);
        articleMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        if (articleMapper.publish(id) == 0) {
            throw new BusinessException("文章不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpublish(Long id) {
        if (articleMapper.unpublish(id) == 0) {
            throw new BusinessException("文章不存在");
        }
    }

    @Override
    public PageResult<ArticleListVO> pageAdmin(ArticleQuery query) {
        return doPage(query);
    }

    @Override
    public PageResult<ArticleListVO> pageFront(ArticleQuery query) {
        query.setStatus(1); // 前台只展示已发布
        return doPage(query);
    }

    @Override
    public ArticleVO getDetail(Long id) {
        ArticleEntity entity = articleMapper.selectDetailById(id);
        if (entity == null || entity.getStatus() == null || entity.getStatus() != 1) {
            throw new BusinessException("文章不存在");
        }
        articleMapper.incrementViewCount(id);
        entity.setViewCount(entity.getViewCount() + 1);
        recordWeeklyHot(id);
        return buildVO(entity);
    }

    @Override
    public List<ArticleListVO> latest(int limit) {
        List<ArticleListVO> list = articleConvert.toListVOList(articleMapper.selectLatest(limit));
        enrich(list);
        return list;
    }

    @Override
    public List<ArticleListVO> hot(int limit, String range) {
        if ("week".equalsIgnoreCase(range)) {
            List<ArticleListVO> weekList = hotOfWeek(limit);
            if (!weekList.isEmpty()) {
                return weekList;
            }
        }
        List<ArticleListVO> list = articleConvert.toListVOList(articleMapper.selectHot(limit));
        enrich(list);
        return list;
    }

    @Override
    public LikeVO like(Long id, String clientId) {
        Integer likeCount = articleMapper.selectPublishedLikeCount(id);
        if (likeCount == null) {
            throw new BusinessException("文章不存在");
        }
        // Redis Set 去重：24 小时内同一客户端只计一次
        String key = LIKE_KEY_PREFIX + id;
        Long added = stringRedisTemplate.opsForSet().add(key, clientId);
        boolean counted = added != null && added > 0;
        if (counted) {
            stringRedisTemplate.expire(key, Duration.ofHours(24));
            articleMapper.incrementLikeCount(id);
        }
        LikeVO vo = new LikeVO();
        vo.setCounted(counted);
        vo.setLikeCount(counted ? likeCount + 1 : likeCount);
        return vo;
    }

    /** 周榜：读取 Redis ZSET 热度排名（Redis 不可用或无数据时返回空，由调用方回退总榜） */
    private List<ArticleListVO> hotOfWeek(int limit) {
        try {
            Set<String> idSet = stringRedisTemplate.opsForZSet()
                    .reverseRange(HOT_WEEK_KEY_PREFIX + currentWeek(), 0, limit - 1);
            if (idSet == null || idSet.isEmpty()) {
                return List.of();
            }
            List<Long> ids = idSet.stream().map(Long::valueOf).toList();
            Map<Long, ArticleEntity> entityMap = articleMapper.selectPublishedByIds(ids).stream()
                    .collect(Collectors.toMap(ArticleEntity::getId, Function.identity()));
            // 按 Redis 排名重排（SQL 不保证顺序）
            List<ArticleEntity> ordered = ids.stream().map(entityMap::get).filter(Objects::nonNull).toList();
            List<ArticleListVO> list = articleConvert.toListVOList(ordered);
            enrich(list);
            return list;
        } catch (Exception e) {
            log.warn("读取周榜失败（Redis 不可用？），回退总榜：{}", e.getMessage());
            return List.of();
        }
    }

    /** 详情浏览时累加周热度（尽力而为，Redis 异常不影响详情主流程） */
    private void recordWeeklyHot(Long articleId) {
        try {
            String key = HOT_WEEK_KEY_PREFIX + currentWeek();
            stringRedisTemplate.opsForZSet().incrementScore(key, String.valueOf(articleId), 1D);
            stringRedisTemplate.expire(key, Duration.ofDays(8));
        } catch (Exception e) {
            log.warn("记录周热度失败（Redis 不可用？）：{}", e.getMessage());
        }
    }

    /** 当前 ISO 周键，如 2026-41 */
    private static String currentWeek() {
        LocalDate now = LocalDate.now();
        int year = now.get(WeekFields.ISO.weekBasedYear());
        int week = now.get(WeekFields.ISO.weekOfWeekBasedYear());
        return year + "-" + String.format("%02d", week);
    }

    /** ArticleApi：供其他模块调用，不递增浏览量、不限状态 */
    @Override
    public ArticleVO getById(Long id) {
        ArticleEntity entity = articleMapper.selectDetailById(id);
        return entity == null ? null : buildVO(entity);
    }

    /** ArticleApi：分类下的文章数（分类删除保护） */
    @Override
    public long countByCategoryId(Long categoryId) {
        return articleMapper.countByCategoryId(categoryId);
    }

    /** ArticleApi：标签下的文章数（标签删除保护） */
    @Override
    public long countByTagId(Long tagId) {
        return articleMapper.countByTagId(tagId);
    }

    @Override
    public ArticleVO getAdminDetail(Long id) {
        return buildVO(requireArticle(id));
    }

    // ==================== 私有方法 ====================

    /** 列表数据补全：分类名 + 评论数（跨模块只走 Api） */
    private void enrich(List<ArticleListVO> list) {
        fillCategoryNames(list);
        fillCommentCounts(list);
    }

    /** 由 comment 模块 Api 补全各文章已通过评论数 */
    private void fillCommentCounts(List<ArticleListVO> list) {
        if (list.isEmpty()) {
            return;
        }
        Map<Long, Long> countMap = commentApi.countByArticleIds(list.stream().map(ArticleListVO::getId).toList());
        list.forEach(vo -> vo.setCommentCount(countMap.getOrDefault(vo.getId(), 0L)));
    }

    /** 近似字数统计：去掉 Markdown 常见标记与空白后的字符数 */
    private static int countWords(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        String plain = content
                .replaceAll("```[\\s\\S]*?```", " ")
                .replaceAll("`[^`]*`", " ")
                .replaceAll("!?\\[[^\\]]*]\\([^)]*\\)", " ")
                .replaceAll("[#>*_~|-]", " ");
        return plain.replaceAll("\\s+", "").length();
    }

    private PageResult<ArticleListVO> doPage(ArticleQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<ArticleEntity> list = articleMapper.selectPageByQuery(query);
        PageInfo<ArticleEntity> pageInfo = new PageInfo<>(list);
        List<ArticleListVO> voList = articleConvert.toListVOList(list);
        enrich(voList);
        return PageResult.of(pageInfo.getTotal(), voList);
    }

    /** 校验文章存在 */
    private ArticleEntity requireArticle(Long id) {
        ArticleEntity entity = articleMapper.selectDetailById(id);
        if (entity == null) {
            throw new BusinessException("文章不存在");
        }
        return entity;
    }

    /** 校验分类存在（可空） */
    private void checkCategory(Long categoryId) {
        if (categoryId != null && categoryApi.getById(categoryId) == null) {
            throw new BusinessException("分类不存在");
        }
    }

    /** 保存标签关联（去重） */
    private void saveTags(Long articleId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        articleTagMapper.batchInsert(articleId, tagIds.stream().distinct().toList());
    }

    /** 由 category 模块 Api 补全分类名 */
    private void fillCategoryNames(List<ArticleListVO> list) {
        if (list.isEmpty()) {
            return;
        }
        Map<Long, String> nameMap = categoryApi.listAll().stream()
                .collect(Collectors.toMap(CategoryVO::getId, CategoryVO::getName));
        list.forEach(vo -> vo.setCategoryName(nameMap.get(vo.getCategoryId())));
    }

    /** 组装详情 VO（分类名 + 标签） */
    private ArticleVO buildVO(ArticleEntity entity) {
        ArticleVO vo = articleConvert.toVO(entity);
        if (entity.getCategoryId() != null) {
            CategoryVO category = categoryApi.getById(entity.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        List<Long> tagIds = articleTagMapper.selectTagIdsByArticleId(entity.getId());
        vo.setTagIds(tagIds);
        if (!tagIds.isEmpty()) {
            List<TagVO> tags = tagApi.listByIds(tagIds);
            vo.setTags(tags.stream().map(TagVO::getName).toList());
        }
        return vo;
    }
}