package com.myblog.toolbox.api.vo;

import lombok.Data;

import java.util.List;

/**
 * 工具箱总览出参（前台一次返回，前端本地筛选）
 */
@Data
public class ToolboxOverviewVO {

    /** 启用的分类（按排序） */
    private List<ToolCategoryVO> categories;

    /** 上架的命令（按分类排序 + 命令排序） */
    private List<ToolCommandVO> commands;

    /** 命令总数 */
    private Integer total;
}
