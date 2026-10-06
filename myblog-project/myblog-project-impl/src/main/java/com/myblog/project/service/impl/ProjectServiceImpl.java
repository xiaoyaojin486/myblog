package com.myblog.project.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.myblog.common.exception.BusinessException;
import com.myblog.common.result.PageResult;
import com.myblog.project.api.ProjectApi;
import com.myblog.project.api.dto.ProjectCreateDTO;
import com.myblog.project.api.dto.ProjectUpdateDTO;
import com.myblog.project.api.vo.ProjectVO;
import com.myblog.project.convert.ProjectConvert;
import com.myblog.project.entity.ProjectEntity;
import com.myblog.project.mapper.ProjectMapper;
import com.myblog.project.query.ProjectQuery;
import com.myblog.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 项目业务实现（同时实现本地 ProjectService 与对外 ProjectApi）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService, ProjectApi {

    private final ProjectMapper projectMapper;
    private final ProjectConvert projectConvert;

    @Override
    public List<ProjectVO> listOnline() {
        return projectConvert.toVOList(projectMapper.selectOnline());
    }

    @Override
    public ProjectVO getDetail(Long id) {
        ProjectEntity entity = projectMapper.selectDetailById(id);
        if (entity == null || entity.getStatus() == null || entity.getStatus() != 1) {
            throw new BusinessException("项目不存在");
        }
        return projectConvert.toVO(entity);
    }

    @Override
    public ProjectVO getAdminDetail(Long id) {
        ProjectEntity entity = projectMapper.selectDetailById(id);
        if (entity == null) {
            throw new BusinessException("项目不存在");
        }
        return projectConvert.toVO(entity);
    }

    @Override
    public PageResult<ProjectVO> pageAdmin(ProjectQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<ProjectEntity> list = projectMapper.selectPageByQuery(query);
        PageInfo<ProjectEntity> pageInfo = new PageInfo<>(list);
        return PageResult.of(pageInfo.getTotal(), projectConvert.toVOList(list));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProjectCreateDTO dto) {
        ProjectEntity entity = projectConvert.toEntity(dto);
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        projectMapper.insert(entity);
        log.info("新增项目：{}（id={}）", entity.getName(), entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ProjectUpdateDTO dto) {
        ProjectEntity entity = projectMapper.selectDetailById(id);
        if (entity == null) {
            throw new BusinessException("项目不存在");
        }
        Integer oldStatus = entity.getStatus();
        projectConvert.updateEntity(dto, entity);
        if (dto.getStatus() == null) {
            entity.setStatus(oldStatus);
        }
        projectMapper.update(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (projectMapper.deleteById(id) == 0) {
            throw new BusinessException("项目不存在");
        }
    }

    /** ProjectApi：供其他模块调用 */
    @Override
    public ProjectVO getById(Long id) {
        return projectConvert.toVO(projectMapper.selectDetailById(id));
    }
}