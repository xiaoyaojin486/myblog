package com.myblog.system.controller;

import com.myblog.common.result.Result;
import com.myblog.system.api.dto.AboutSaveDTO;
import com.myblog.system.api.vo.AboutVO;
import com.myblog.system.service.AboutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 关于我接口（前台展示 + 后台管理）
 * 注意：统一前缀 /api 由 context-path 提供，此处不重复写
 */
@RestController
@RequiredArgsConstructor
public class AboutController {

    private final AboutService aboutService;

    /** 关于我内容（前台展示） */
    @GetMapping("/about")
    public Result<AboutVO> about() {
        return Result.success(aboutService.getAbout());
    }

    /** 关于我内容（后台编辑回填） */
    @GetMapping("/admin/about")
    public Result<AboutVO> adminAbout() {
        return Result.success(aboutService.getAbout());
    }

    /** 保存关于我（后台，技能栈与经历全量覆盖） */
    @PutMapping("/admin/about")
    public Result<Void> save(@RequestBody @Valid AboutSaveDTO dto) {
        aboutService.saveAbout(dto);
        return Result.success();
    }
}