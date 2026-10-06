package com.myblog.tag.controller;

import com.myblog.common.result.Result;
import com.myblog.tag.api.dto.TagCreateDTO;
import com.myblog.tag.api.dto.TagUpdateDTO;
import com.myblog.tag.api.vo.TagVO;
import com.myblog.tag.service.TagService;
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
 * 标签接口
 * 注意：统一前缀 /api 由 context-path 提供，此处不重复写
 */
@RestController
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /** 标签列表（前台/后台；keyword 可选，按名称搜索） */
    @GetMapping("/tag/list")
    public Result<List<TagVO>> list(@RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(tagService.listAll(keyword));
    }

    /** 新增标签（后台） */
    @PostMapping("/admin/tag")
    public Result<Long> create(@RequestBody @Valid TagCreateDTO dto) {
        return Result.success(tagService.create(dto));
    }

    /** 更新标签（后台） */
    @PutMapping("/admin/tag/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody @Valid TagUpdateDTO dto) {
        tagService.update(id, dto);
        return Result.success();
    }

    /** 删除标签（后台，已被文章使用时不可删除） */
    @DeleteMapping("/admin/tag/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        tagService.delete(id);
        return Result.success();
    }
}