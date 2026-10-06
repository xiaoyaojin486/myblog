package com.myblog.tag.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签表实体（tag）
 * 注意：Entity 只在 Service/Mapper 内部使用，不返回前端
 */
@Data
public class TagEntity {

    private Long id;

    private String name;

    private LocalDateTime createTime;
}