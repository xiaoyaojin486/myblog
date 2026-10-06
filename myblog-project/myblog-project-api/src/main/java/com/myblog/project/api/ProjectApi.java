package com.myblog.project.api;

import com.myblog.project.api.vo.ProjectVO;

import java.util.List;

/**
 * 项目展示模块对外接口
 * 注意：跨模块调用只能注入本接口，禁止依赖 myblog-project-impl 的内部实现
 */
public interface ProjectApi {

    /**
     * 全部上架项目（按排序）
     *
     * @return 项目列表（不含详情内容）
     */
    List<ProjectVO> listOnline();

    /**
     * 根据 ID 查询项目
     *
     * @param id 项目 ID
     * @return 项目信息（不存在时返回 null）
     */
    ProjectVO getById(Long id);
}