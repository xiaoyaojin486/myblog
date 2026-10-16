package com.myblog.toolbox.controller;

import com.myblog.common.result.PageResult;
import com.myblog.common.result.Result;
import com.myblog.toolbox.api.dto.ToolCategoryDTO;
import com.myblog.toolbox.api.dto.ToolCommandCreateDTO;
import com.myblog.toolbox.api.dto.ToolCommandUpdateDTO;
import com.myblog.toolbox.api.vo.ToolCategoryVO;
import com.myblog.toolbox.api.vo.ToolCommandVO;
import com.myblog.toolbox.query.ToolCommandQuery;
import com.myblog.toolbox.service.ToolboxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 运维工具箱后台接口（/admin/**，需 JWT）
 */
@RestController
@RequiredArgsConstructor
public class ToolboxAdminController {

    private final ToolboxService toolboxService;

    // ===================== 命令 =====================

    /** 命令分页查询 */
    @GetMapping("/admin/toolbox/page")
    public Result<PageResult<ToolCommandVO>> page(ToolCommandQuery query) {
        return Result.success(toolboxService.pageCommands(query));
    }

    /** 命令详情（编辑回填用，下架可查） */
    @GetMapping("/admin/toolbox/{id}")
    public Result<ToolCommandVO> detail(@PathVariable("id") Long id) {
        return Result.success(toolboxService.getCommand(id));
    }

    /** 新增命令 */
    @PostMapping("/admin/toolbox")
    public Result<Long> create(@RequestBody @Valid ToolCommandCreateDTO dto) {
        return Result.success(toolboxService.createCommand(dto));
    }

    /** 更新命令 */
    @PutMapping("/admin/toolbox/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody @Valid ToolCommandUpdateDTO dto) {
        toolboxService.updateCommand(id, dto);
        return Result.success();
    }

    /** 删除命令 */
    @DeleteMapping("/admin/toolbox/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        toolboxService.deleteCommand(id);
        return Result.success();
    }

    // ===================== 分类 =====================

    /** 全部分类 */
    @GetMapping("/admin/toolbox/category/list")
    public Result<List<ToolCategoryVO>> categoryList() {
        return Result.success(toolboxService.listCategories());
    }

    /** 新增分类 */
    @PostMapping("/admin/toolbox/category")
    public Result<Long> createCategory(@RequestBody @Valid ToolCategoryDTO dto) {
        return Result.success(toolboxService.createCategory(dto));
    }

    /** 更新分类 */
    @PutMapping("/admin/toolbox/category/{id}")
    public Result<Void> updateCategory(@PathVariable("id") Long id, @RequestBody @Valid ToolCategoryDTO dto) {
        toolboxService.updateCategory(id, dto);
        return Result.success();
    }

    /** 删除分类 */
    @DeleteMapping("/admin/toolbox/category/{id}")
    public Result<Void> deleteCategory(@PathVariable("id") Long id) {
        toolboxService.deleteCategory(id);
        return Result.success();
    }
}
