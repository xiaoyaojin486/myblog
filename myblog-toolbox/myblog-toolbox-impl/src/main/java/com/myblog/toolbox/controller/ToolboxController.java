package com.myblog.toolbox.controller;

import com.myblog.common.result.Result;
import com.myblog.toolbox.api.vo.ToolboxOverviewVO;
import com.myblog.toolbox.service.ToolboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运维工具箱前台接口
 * 注意：统一前缀 /api 由 context-path 提供，此处不重复写
 */
@RestController
@RequiredArgsConstructor
public class ToolboxController {

    private final ToolboxService toolboxService;

    /** 工具箱总览（启用分类 + 上架命令），一次返回，前端本地筛选 */
    @GetMapping("/toolbox/overview")
    public Result<ToolboxOverviewVO> overview() {
        return Result.success(toolboxService.overview());
    }
}
