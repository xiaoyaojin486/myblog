package com.myblog.project.controller;

import com.myblog.common.result.PageResult;
import com.myblog.common.result.Result;
import com.myblog.project.api.dto.ProjectCreateDTO;
import com.myblog.project.api.dto.ProjectUpdateDTO;
import com.myblog.project.api.vo.ProjectVO;
import com.myblog.project.query.ProjectQuery;
import com.myblog.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 项目展示接口（前台展示 + 后台管理）
 * 注意：统一前缀 /api 由 context-path 提供，此处不重复写
 */
@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /** 上架项目列表（前台，可按进度筛选：0规划中,1进行中,2已完成,3已暂停） */
    @GetMapping("/project/list")
    public Result<List<ProjectVO>> list(@RequestParam(value = "progress", required = false) Integer progress) {
        return Result.success(projectService.listOnline(progress));
    }

    /** 项目详情（前台） */
    @GetMapping("/project/{id}")
    public Result<ProjectVO> detail(@PathVariable("id") Long id) {
        return Result.success(projectService.getDetail(id));
    }

    /** 分页查询（后台） */
    @GetMapping("/admin/project/page")
    public Result<PageResult<ProjectVO>> page(ProjectQuery query) {
        return Result.success(projectService.pageAdmin(query));
    }

    /** 项目详情（后台编辑回填用，下架可查） */
    @GetMapping("/admin/project/{id}")
    public Result<ProjectVO> adminDetail(@PathVariable("id") Long id) {
        return Result.success(projectService.getAdminDetail(id));
    }

    /** 新增项目（后台） */
    @PostMapping("/admin/project")
    public Result<Long> create(@RequestBody @Valid ProjectCreateDTO dto) {
        return Result.success(projectService.create(dto));
    }

    /** 更新项目（后台） */
    @PutMapping("/admin/project/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody @Valid ProjectUpdateDTO dto) {
        projectService.update(id, dto);
        return Result.success();
    }

    /** 删除项目（后台） */
    @DeleteMapping("/admin/project/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        projectService.delete(id);
        return Result.success();
    }
}