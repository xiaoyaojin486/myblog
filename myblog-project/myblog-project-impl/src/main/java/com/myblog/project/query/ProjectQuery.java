package com.myblog.project.query;

import lombok.Data;

/**
 * 项目分页查询条件（模块内部使用）
 */
@Data
public class ProjectQuery {

    /** 页码（从 1 开始） */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 关键字（匹配项目名） */
    private String keyword;

    /** 状态:0下架,1上架 */
    private Integer status;
}