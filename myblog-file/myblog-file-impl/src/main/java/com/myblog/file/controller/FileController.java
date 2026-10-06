package com.myblog.file.controller;

import com.myblog.common.result.PageResult;
import com.myblog.common.result.Result;
import com.myblog.file.api.dto.FileQuery;
import com.myblog.file.api.dto.FileRenameDTO;
import com.myblog.file.api.vo.FileUploadVO;
import com.myblog.file.api.vo.FileVO;
import com.myblog.file.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件接口（管理端专用，图片存储于阿里云 OSS）
 * 注意：统一前缀 /api 由 context-path 提供，此处不重复写
 * 整个 /admin/** 由 JwtAuthFilter 统一鉴权
 */
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /** 上传图片（bizType：avatar / article / project / moment / other） */
    @PostMapping("/admin/file/upload")
    public Result<FileUploadVO> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "bizType", defaultValue = "other") String bizType) {
        return Result.success(fileService.upload(file, bizType));
    }

    /** 分页查询 OSS 文件（仅 myblog/ 目录，支持业务类型与文件名筛选） */
    @GetMapping("/admin/file/page")
    public Result<PageResult<FileVO>> page(FileQuery query) {
        return Result.success(fileService.page(query));
    }

    /** 重命名 / 移动 OSS 文件（内部为 copy + delete） */
    @PutMapping("/admin/file/rename")
    public Result<Void> rename(@RequestBody @Valid FileRenameDTO dto) {
        fileService.rename(dto);
        return Result.success();
    }

    /** 删除 OSS 文件（按 objectKey，仅允许 myblog/ 目录） */
    @DeleteMapping("/admin/file")
    public Result<Void> delete(@RequestParam("objectKey") String objectKey) {
        fileService.delete(objectKey);
        return Result.success();
    }

    /** 批量删除 OSS 文件（单次上限 100） */
    @DeleteMapping("/admin/file/batch")
    public Result<Void> deleteBatch(@RequestBody List<String> objectKeys) {
        fileService.deleteBatch(objectKeys);
        return Result.success();
    }
}