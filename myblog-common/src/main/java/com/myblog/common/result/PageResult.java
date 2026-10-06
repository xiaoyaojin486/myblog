package com.myblog.common.result;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 *
 * @param <T> 记录类型
 */
@Data
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private Long total;

    /** 当前页数据 */
    private List<T> records;

    public static <T> PageResult<T> of(Long total, List<T> records) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setRecords(records);
        return pageResult;
    }
}