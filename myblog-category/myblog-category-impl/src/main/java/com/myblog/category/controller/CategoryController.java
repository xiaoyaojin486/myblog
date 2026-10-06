package com.myblog.category.controller;

import com.myblog.category.api.dto.CategoryCreateDTO;
import com.myblog.category.api.dto.CategoryUpdateDTO;
import com.myblog.category.api.vo.CategoryVO;
import com.myblog.category.service.CategoryService;
import com.myblog.common.result.Result;
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
 * 分类接口
 * 注意：统一前缀 /api 由 context-path 提供，此处不重复写
 */
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /** 分类列表（前台/后台；keyword 可选，按名称搜索） */
    @GetMapping("/category/list")
    public Result<List<CategoryVO>> list(@RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(categoryService.listAll(keyword));
    }

    /** 新增分类（后台） */
    @PostMapping("/admin/category")
    public Result<Long> create(@RequestBody @Valid CategoryCreateDTO dto) {
        return Result.success(categoryService.create(dto));
    }

    /** 更新分类（后台） */
    @PutMapping("/admin/category/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody @Valid CategoryUpdateDTO dto) {
        categoryService.update(id, dto);
        return Result.success();
    }

    /** 删除分类（后台） */
    @DeleteMapping("/admin/category/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}