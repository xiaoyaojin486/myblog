package com.myblog.toolbox.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.myblog.common.exception.BusinessException;
import com.myblog.common.result.PageResult;
import com.myblog.toolbox.api.dto.ToolCategoryDTO;
import com.myblog.toolbox.api.dto.ToolCommandCreateDTO;
import com.myblog.toolbox.api.dto.ToolCommandUpdateDTO;
import com.myblog.toolbox.api.vo.ToolCategoryVO;
import com.myblog.toolbox.api.vo.ToolCommandVO;
import com.myblog.toolbox.api.vo.ToolboxOverviewVO;
import com.myblog.toolbox.convert.ToolCategoryConvert;
import com.myblog.toolbox.convert.ToolCommandConvert;
import com.myblog.toolbox.entity.ToolCategoryEntity;
import com.myblog.toolbox.entity.ToolCommandEntity;
import com.myblog.toolbox.mapper.ToolCategoryMapper;
import com.myblog.toolbox.mapper.ToolCommandMapper;
import com.myblog.toolbox.query.ToolCommandQuery;
import com.myblog.toolbox.service.ToolboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 运维工具箱业务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolboxServiceImpl implements ToolboxService {

    /** 风险默认值：只读 */
    private static final int RISK_READ_ONLY = 0;

    /** 状态默认值：上架/启用 */
    private static final int STATUS_ENABLED = 1;

    private final ToolCommandMapper toolCommandMapper;
    private final ToolCategoryMapper toolCategoryMapper;
    private final ToolCommandConvert toolCommandConvert;
    private final ToolCategoryConvert toolCategoryConvert;

    @Override
    public ToolboxOverviewVO overview() {
        List<ToolCommandVO> commands = toolCommandConvert.toVOList(toolCommandMapper.selectOnline());
        ToolboxOverviewVO vo = new ToolboxOverviewVO();
        vo.setCategories(toolCategoryConvert.toVOList(toolCategoryMapper.selectEnabled()));
        vo.setCommands(commands);
        vo.setTotal(commands.size());
        return vo;
    }

    @Override
    public PageResult<ToolCommandVO> pageCommands(ToolCommandQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<ToolCommandEntity> list = toolCommandMapper.selectPageByQuery(query);
        PageInfo<ToolCommandEntity> pageInfo = new PageInfo<>(list);
        return PageResult.of(pageInfo.getTotal(), toolCommandConvert.toVOList(list));
    }

    @Override
    public ToolCommandVO getCommand(Long id) {
        ToolCommandEntity entity = toolCommandMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("命令不存在");
        }
        return toolCommandConvert.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCommand(ToolCommandCreateDTO dto) {
        ensureCategoryExists(dto.getCategoryId());
        ToolCommandEntity entity = toolCommandConvert.toEntity(dto);
        if (entity.getRiskLevel() == null) {
            entity.setRiskLevel(RISK_READ_ONLY);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(STATUS_ENABLED);
        }
        toolCommandMapper.insert(entity);
        log.info("新增工具箱命令：{}（id={}）", entity.getName(), entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCommand(Long id, ToolCommandUpdateDTO dto) {
        ToolCommandEntity entity = toolCommandMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("命令不存在");
        }
        ensureCategoryExists(dto.getCategoryId());
        // 记录原值：可选字段为空时保持不变
        Integer oldRisk = entity.getRiskLevel();
        Integer oldSort = entity.getSort();
        Integer oldStatus = entity.getStatus();
        toolCommandConvert.updateEntity(dto, entity);
        if (dto.getRiskLevel() == null) {
            entity.setRiskLevel(oldRisk);
        }
        if (dto.getSort() == null) {
            entity.setSort(oldSort);
        }
        if (dto.getStatus() == null) {
            entity.setStatus(oldStatus);
        }
        toolCommandMapper.update(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommand(Long id) {
        if (toolCommandMapper.deleteById(id) == 0) {
            throw new BusinessException("命令不存在");
        }
    }

    @Override
    public List<ToolCategoryVO> listCategories() {
        return toolCategoryConvert.toVOList(toolCategoryMapper.selectAll());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(ToolCategoryDTO dto) {
        ToolCategoryEntity entity = toolCategoryConvert.toEntity(dto);
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(STATUS_ENABLED);
        }
        toolCategoryMapper.insert(entity);
        log.info("新增工具箱分类：{}（id={}）", entity.getName(), entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, ToolCategoryDTO dto) {
        ToolCategoryEntity entity = toolCategoryMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("分类不存在");
        }
        Integer oldSort = entity.getSort();
        Integer oldStatus = entity.getStatus();
        toolCategoryConvert.updateEntity(dto, entity);
        if (dto.getSort() == null) {
            entity.setSort(oldSort);
        }
        if (dto.getStatus() == null) {
            entity.setStatus(oldStatus);
        }
        toolCategoryMapper.update(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        ToolCategoryEntity entity = toolCategoryMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("分类不存在");
        }
        long count = toolCommandMapper.countByCategory(id);
        if (count > 0) {
            throw new BusinessException("该分类下仍有 " + count + " 条命令，无法删除");
        }
        toolCategoryMapper.deleteById(id);
    }

    /** 校验分类存在（避免命令挂到不存在的分类） */
    private void ensureCategoryExists(Long categoryId) {
        if (categoryId == null || toolCategoryMapper.selectById(categoryId) == null) {
            throw new BusinessException("分类不存在");
        }
    }
}
