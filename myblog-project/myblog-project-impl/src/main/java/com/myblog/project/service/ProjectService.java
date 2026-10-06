package com.myblog.project.service;

import com.myblog.common.result.PageResult;
import com.myblog.project.api.dto.ProjectCreateDTO;
import com.myblog.project.api.dto.ProjectUpdateDTO;
import com.myblog.project.api.vo.ProjectVO;
import com.myblog.project.query.ProjectQuery;

import java.util.List;

/**
 * 项目业务接口（模块内部使用）
 */
public interface ProjectService {

    /** 前台：上架项目列表（progress 非空时按进度筛选） */
    List<ProjectVO> listOnline(Integer progress);

    /** 前台：项目详情（仅上架） */
    ProjectVO getDetail(Long id);

    /** 后台：项目详情（下架也可查看，编辑回填用） */
    ProjectVO getAdminDetail(Long id);

    /** 后台：分页查询 */
    PageResult<ProjectVO> pageAdmin(ProjectQuery query);

    /** 后台：新增 */
    Long create(ProjectCreateDTO dto);

    /** 后台：更新 */
    void update(Long id, ProjectUpdateDTO dto);

    /** 后台：删除 */
    void delete(Long id);
}