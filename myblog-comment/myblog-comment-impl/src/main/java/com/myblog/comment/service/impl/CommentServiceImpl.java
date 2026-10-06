package com.myblog.comment.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.myblog.article.api.ArticleApi;
import com.myblog.article.api.vo.ArticleVO;
import com.myblog.comment.api.CommentApi;
import com.myblog.comment.api.dto.CommentCreateDTO;
import com.myblog.comment.api.vo.AdminCommentVO;
import com.myblog.comment.api.vo.CommentVO;
import com.myblog.comment.convert.CommentConvert;
import com.myblog.comment.entity.CommentCountEntity;
import com.myblog.comment.entity.CommentEntity;
import com.myblog.comment.mapper.CommentMapper;
import com.myblog.comment.query.CommentQuery;
import com.myblog.comment.service.CommentService;
import com.myblog.common.exception.BusinessException;
import com.myblog.common.result.PageResult;
import com.myblog.common.security.UserContext;
import com.myblog.user.api.UserApi;
import com.myblog.user.api.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 评论业务实现（同时实现本地 CommentService 与对外 CommentApi）
 * 跨模块调用：注入 ArticleApi（校验文章与回填标题）与 UserApi（取博主昵称）；
 * article-impl 反向依赖 comment-api 统计评论数，因此这里用 @Lazy 延迟注入打破 Spring bean 循环依赖
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService, CommentApi {

    /** 评论状态：0待审核，1通过，2拒绝 */
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_REJECTED = 2;

    /** 批量操作单次上限 */
    private static final int MAX_BATCH_SIZE = 100;

    private final CommentMapper commentMapper;
    private final CommentConvert commentConvert;
    private final ArticleApi articleApi;
    private final UserApi userApi;

    public CommentServiceImpl(CommentMapper commentMapper, CommentConvert commentConvert,
                              @Lazy ArticleApi articleApi, UserApi userApi) {
        this.commentMapper = commentMapper;
        this.commentConvert = commentConvert;
        this.articleApi = articleApi;
        this.userApi = userApi;
    }

    @Override
    public List<CommentVO> listByArticle(Long articleId) {
        List<CommentVO> voList = commentConvert.toVOList(commentMapper.selectApprovedByArticleId(articleId));
        Map<Long, CommentVO> voMap = voList.stream()
                .collect(Collectors.toMap(CommentVO::getId, Function.identity()));
        List<CommentVO> roots = new ArrayList<>();
        for (CommentVO vo : voList) {
            CommentVO parent = vo.getParentId() == null ? null : voMap.get(vo.getParentId());
            if (parent == null) {
                roots.add(vo);
            } else {
                parent.getChildren().add(vo);
            }
        }
        return roots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CommentCreateDTO dto, String ip) {
        ArticleVO article = articleApi.getById(dto.getArticleId());
        if (article == null || article.getStatus() == null || article.getStatus() != 1) {
            throw new BusinessException("文章不存在");
        }
        if (dto.getParentId() != null) {
            CommentEntity parent = commentMapper.selectById(dto.getParentId());
            if (parent == null || !parent.getArticleId().equals(dto.getArticleId())) {
                throw new BusinessException("回复的评论不存在");
            }
        }
        CommentEntity entity = commentConvert.toEntity(dto);
        entity.setStatus(STATUS_PENDING); // 匿名提交先待审核，后台通过后展示
        entity.setIp(ip);
        commentMapper.insert(entity);
        log.info("新增评论：articleId={}，nickname={}，ip={}", dto.getArticleId(), dto.getNickname(), ip);
    }

    @Override
    public PageResult<AdminCommentVO> pageAdmin(CommentQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<CommentEntity> list = commentMapper.selectPageByQuery(query);
        PageInfo<CommentEntity> pageInfo = new PageInfo<>(list);
        List<AdminCommentVO> voList = commentConvert.toAdminVOList(list);
        fillArticleTitles(voList);
        return PageResult.of(pageInfo.getTotal(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        changeStatus(id, STATUS_APPROVED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id) {
        changeStatus(id, STATUS_REJECTED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatusBatch(List<Long> ids, Integer status) {
        if (status == null || (status != STATUS_APPROVED && status != STATUS_REJECTED)) {
            throw new BusinessException("不支持的状态");
        }
        checkBatch(ids);
        commentMapper.updateStatusBatch(ids, status);
        log.info("批量审核评论：count={}，status={}", ids.size(), status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reply(Long commentId, String content) {
        CommentEntity parent = commentMapper.selectById(commentId);
        if (parent == null) {
            throw new BusinessException("评论不存在");
        }
        CommentEntity entity = new CommentEntity();
        entity.setArticleId(parent.getArticleId());
        entity.setParentId(parent.getId());
        entity.setUserId(UserContext.getUserId()); // 标记为博主发布，前台显示「博主」标识
        entity.setNickname(currentNickname());
        entity.setContent(content);
        entity.setStatus(STATUS_APPROVED); // 博主回复无需审核，直接展示
        commentMapper.insert(entity);
        log.info("博主回复评论：parentId={}，articleId={}", commentId, parent.getArticleId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (commentMapper.deleteById(id) == 0) {
            throw new BusinessException("评论不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        checkBatch(ids);
        commentMapper.deleteByIds(ids);
        log.info("批量删除评论：count={}", ids.size());
    }

    /** CommentApi：批量统计各文章已通过评论数（文章列表展示） */
    @Override
    public Map<Long, Long> countByArticleIds(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return Map.of();
        }
        return commentMapper.countApprovedByArticleIds(articleIds).stream()
                .collect(Collectors.toMap(CommentCountEntity::getArticleId, CommentCountEntity::getTotal));
    }

    // ==================== 私有方法 ====================

    /** 单个改状态（先确认存在） */
    private void changeStatus(Long id, int status) {
        if (commentMapper.selectById(id) == null) {
            throw new BusinessException("评论不存在");
        }
        commentMapper.updateStatus(id, status);
    }

    /** 批量操作前校验：非空且不超过上限 */
    private void checkBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要操作的评论");
        }
        if (ids.size() > MAX_BATCH_SIZE) {
            throw new BusinessException("单次最多操作 " + MAX_BATCH_SIZE + " 条评论");
        }
    }

    /** 当前登录博主的昵称（取不到时回退为「博主」） */
    private String currentNickname() {
        Long userId = UserContext.getUserId();
        if (userId != null) {
            UserVO user = userApi.getById(userId);
            if (user != null && user.getNickname() != null && !user.getNickname().isBlank()) {
                return user.getNickname();
            }
        }
        return "博主";
    }

    /** 由 article 模块 Api 补全文章标题 */
    private void fillArticleTitles(List<AdminCommentVO> list) {
        if (list.isEmpty()) {
            return;
        }
        Map<Long, String> titleMap = new HashMap<>();
        for (AdminCommentVO vo : list) {
            if (!titleMap.containsKey(vo.getArticleId())) {
                ArticleVO article = articleApi.getById(vo.getArticleId());
                titleMap.put(vo.getArticleId(), article == null ? "文章已删除" : article.getTitle());
            }
            vo.setArticleTitle(titleMap.get(vo.getArticleId()));
        }
    }
}