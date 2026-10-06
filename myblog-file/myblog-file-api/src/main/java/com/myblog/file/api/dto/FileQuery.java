package com.myblog.file.api.dto;

import lombok.Data;

/**
 * OSS 文件分页查询入参
 */
@Data
public class FileQuery {

    /** 业务类型（可选）：avatar / article / project / moment / other */
    private String bizType;

    /** 文件名关键字（可选，不区分大小写） */
    private String keyword;

    /** 页码，从 1 开始 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;
}