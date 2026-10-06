package com.myblog.category.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类表实体（category）
 * 注意：Entity 只在 Service/Mapper 内部使用，不返回前端
 */
@Data
public class CategoryEntity {

    private Long id;

    private String name;

    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}