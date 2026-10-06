package com.myblog.file.service;

import com.myblog.common.result.PageResult;
import com.myblog.file.api.dto.FileQuery;
import com.myblog.file.api.dto.FileRenameDTO;
import com.myblog.file.api.vo.FileUploadVO;
import com.myblog.file.api.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件业务接口（模块内部使用）
 */
public interface FileService {

    /**
     * 上传图片到 OSS
     *
     * @param file    图片文件
     * @param bizType 业务类型：avatar（头像）/ article（文章图片）/ moment（动态）/ other
     * @return 可访问 URL 与 objectKey
     */
    FileUploadVO upload(MultipartFile file, String bizType);

    /**
     * 分页查询 OSS 文件（仅 myblog/ 目录下）
     *
     * @param query 业务类型 / 文件名关键字 / 分页参数
     * @return 分页结果
     */
    PageResult<FileVO> page(FileQuery query);

    /**
     * 重命名 / 移动 OSS 文件（OSS 无原地重命名，实现为 copy + delete）
     *
     * @param dto 原 Key、新文件名、目标业务类型目录（可选）
     */
    void rename(FileRenameDTO dto);

    /**
     * 删除 OSS 文件
     *
     * @param objectKey 对象 Key（必须以 myblog/ 开头）
     */
    void delete(String objectKey);

    /**
     * 批量删除 OSS 文件
     *
     * @param objectKeys 对象 Key 列表（每项均须以 myblog/ 开头）
     */
    void deleteBatch(List<String> objectKeys);
}