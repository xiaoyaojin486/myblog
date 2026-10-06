package com.myblog.file.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.ListObjectsV2Request;
import com.aliyun.oss.model.ListObjectsV2Result;
import com.aliyun.oss.model.OSSObjectSummary;
import com.myblog.common.exception.BusinessException;
import com.myblog.common.result.PageResult;
import com.myblog.common.security.UserContext;
import com.myblog.file.api.dto.FileQuery;
import com.myblog.file.api.dto.FileRenameDTO;
import com.myblog.file.api.vo.FileUploadVO;
import com.myblog.file.api.vo.FileVO;
import com.myblog.file.config.OssProperties;
import com.myblog.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文件业务实现
 * OSS 目录规范：myblog/{用户ID}/{业务类型}/yyyy/MM/{uuid}.{ext}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    /** 允许的图片扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    /** 允许的业务类型（对应用户目录下的子目录） */
    private static final Set<String> ALLOWED_BIZ_TYPES = Set.of("avatar", "article", "project", "moment", "other");

    /** 单文件大小上限（与 yaml 中 multipart 配置一致） */
    private static final long MAX_SIZE = 10 * 1024 * 1024;

    /** 单次列举请求拉取的对象数 */
    private static final int LIST_BATCH_SIZE = 1000;

    /** 列表扫描安全上限：最多统计这么多个对象 */
    private static final int MAX_LIST_SIZE = 5000;

    /** 批量删除单次上限 */
    private static final int MAX_BATCH_SIZE = 100;

    /** 每页条数上限 */
    private static final int MAX_PAGE_SIZE = 100;

    /** 列表排序：最后修改时间倒序（空值排最后） */
    private static final Comparator<FileVO> BY_TIME_DESC = Comparator.comparing(
            FileVO::getLastModified,
            Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder()));

    private final OSS ossClient;
    private final OssProperties ossProperties;

    @Override
    public FileUploadVO upload(MultipartFile file, String bizType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("图片大小不能超过 10MB");
        }
        String ext = resolveExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException("仅支持 jpg/jpeg/png/gif/webp 格式图片");
        }
        String type = ALLOWED_BIZ_TYPES.contains(bizType) ? bizType : "other";
        String objectKey = buildObjectKey(type, ext);
        try {
            ossClient.putObject(ossProperties.getBucketName(), objectKey, file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException("图片读取失败");
        } catch (OSSException | ClientException e) {
            log.error("OSS 上传失败：{}", objectKey, e);
            throw new BusinessException("图片上传失败，请稍后重试");
        }
        FileUploadVO vo = new FileUploadVO();
        vo.setObjectKey(objectKey);
        vo.setUrl(buildUrl(objectKey));
        log.info("图片上传成功：{}", objectKey);
        return vo;
    }

    @Override
    public PageResult<FileVO> page(FileQuery query) {
        // OSS 只有 continuation-token 翻页、没有 offset，故先按前缀拉全量再本地过滤分页。
        // 单管理员博客量级（几十~几百）足够；若将来上万，改为 token 分页。
        List<OSSObjectSummary> summaries = listAll();
        String bizType = trimToNull(query.getBizType());
        String keyword = trimToNull(query.getKeyword());

        List<FileVO> matched = summaries.stream()
                .filter((s) -> !s.getKey().endsWith("/")) // 跳过目录占位对象
                .map(this::toVO)
                .filter((vo) -> bizType == null || bizType.equals(vo.getBizType()))
                .filter((vo) -> keyword == null
                        || vo.getName().toLowerCase().contains(keyword.toLowerCase()))
                .sorted(BY_TIME_DESC) // 新的排前面
                .collect(Collectors.toCollection(ArrayList::new));

        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1
                ? 10 : Math.min(query.getPageSize(), MAX_PAGE_SIZE);
        int from = (pageNum - 1) * pageSize;
        List<FileVO> records = from >= matched.size()
                ? List.of()
                : new ArrayList<>(matched.subList(from, Math.min(from + pageSize, matched.size())));
        return PageResult.of((long) matched.size(), records);
    }

    @Override
    public void rename(FileRenameDTO dto) {
        String sourceKey = dto.getObjectKey();
        assertOwned(sourceKey);
        String newKey = buildRenamedKey(sourceKey, dto.getNewName(), dto.getTargetBizType());
        if (newKey.equals(sourceKey)) {
            return;
        }
        String bucket = ossProperties.getBucketName();
        try {
            if (ossClient.doesObjectExist(bucket, newKey)) {
                throw new BusinessException("目标文件名已存在");
            }
            // OSS 无原地重命名：先复制到新 Key，再删除旧 Key
            ossClient.copyObject(bucket, sourceKey, bucket, newKey);
            ossClient.deleteObject(bucket, sourceKey);
        } catch (OSSException | ClientException e) {
            log.error("OSS 重命名失败：{} -> {}", sourceKey, newKey, e);
            throw new BusinessException("重命名失败，请稍后重试");
        }
        log.info("OSS 重命名成功：{} -> {}", sourceKey, newKey);
    }

    @Override
    public void delete(String objectKey) {
        assertOwned(objectKey);
        try {
            ossClient.deleteObject(ossProperties.getBucketName(), objectKey);
        } catch (OSSException | ClientException e) {
            log.error("OSS 删除失败：{}", objectKey, e);
            throw new BusinessException("图片删除失败，请稍后重试");
        }
    }

    @Override
    public void deleteBatch(List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) {
            throw new BusinessException("请选择要删除的文件");
        }
        if (objectKeys.size() > MAX_BATCH_SIZE) {
            throw new BusinessException("单次最多删除 " + MAX_BATCH_SIZE + " 个文件");
        }
        objectKeys.forEach(this::assertOwned);
        try {
            DeleteObjectsRequest request = new DeleteObjectsRequest(ossProperties.getBucketName());
            request.setKeys(objectKeys);
            ossClient.deleteObjects(request);
        } catch (OSSException | ClientException e) {
            log.error("OSS 批量删除失败：{}", objectKeys, e);
            throw new BusinessException("删除失败，请稍后重试");
        }
        log.info("OSS 批量删除成功，共 {} 个", objectKeys.size());
    }

    // ==================== 私有方法 ====================

    /** 越权校验：只允许操作 myblog/ 目录下的对象 */
    private void assertOwned(String objectKey) {
        if (objectKey == null || !objectKey.startsWith(ossProperties.getDirPrefix())) {
            throw new BusinessException("非法的文件路径");
        }
    }

    /** 按前缀循环列举对象（continuation-token 翻页，带上限保护） */
    private List<OSSObjectSummary> listAll() {
        List<OSSObjectSummary> summaries = new ArrayList<>();
        String continuationToken = null;
        try {
            do {
                ListObjectsV2Request request = new ListObjectsV2Request(ossProperties.getBucketName());
                request.setPrefix(ossProperties.getDirPrefix());
                request.setMaxKeys(LIST_BATCH_SIZE);
                request.setContinuationToken(continuationToken);
                ListObjectsV2Result result = ossClient.listObjectsV2(request);
                summaries.addAll(result.getObjectSummaries());
                continuationToken = result.isTruncated() ? result.getNextContinuationToken() : null;
            } while (continuationToken != null && summaries.size() < MAX_LIST_SIZE);
        } catch (OSSException | ClientException e) {
            log.error("OSS 列举文件失败", e);
            throw new BusinessException("读取文件列表失败，请稍后重试");
        }
        return summaries;
    }

    /** OSS 对象摘要 -> 出参 */
    private FileVO toVO(OSSObjectSummary summary) {
        String objectKey = summary.getKey();
        FileVO vo = new FileVO();
        vo.setObjectKey(objectKey);
        vo.setUrl(buildUrl(objectKey));
        vo.setName(objectKey.substring(objectKey.lastIndexOf('/') + 1));
        vo.setExt(resolveExtension(objectKey));
        vo.setBizType(parseBizType(objectKey));
        vo.setSize(summary.getSize());
        if (summary.getLastModified() != null) {
            vo.setLastModified(LocalDateTime.ofInstant(summary.getLastModified().toInstant(),
                    ZoneId.systemDefault()));
        }
        return vo;
    }

    /** 从 objectKey 解析业务类型：myblog/{userId}/{bizType}/... */
    private String parseBizType(String objectKey) {
        String[] parts = objectKey.split("/");
        return parts.length > 2 ? parts[2] : "other";
    }

    /**
     * 拼接新 objectKey：保持 myblog/{userId}/ 与 yyyy/MM/ 层级不变，
     * 只替换文件名，可选切换 bizType 目录；扩展名强制沿用原文件
     */
    private String buildRenamedKey(String sourceKey, String newName, String targetBizType) {
        String[] parts = sourceKey.split("/");
        // myblog / {userId} / {bizType} / yyyy / MM / {file}
        if (parts.length != 6 || !ALLOWED_BIZ_TYPES.contains(parts[2])) {
            throw new BusinessException("非法的文件路径");
        }
        parts[2] = ALLOWED_BIZ_TYPES.contains(targetBizType) ? targetBizType : parts[2];
        String fileName = buildFileName(newName, resolveExtension(sourceKey));
        return String.join("/", Arrays.copyOf(parts, parts.length - 1)) + "/" + fileName;
    }

    /** 清洗新文件名（去掉路径分隔符等非法字符），并强制沿用原扩展名 */
    private String buildFileName(String newName, String ext) {
        String base = newName.trim().replaceAll("[\\\\/:*?\"<>|\\s]", "_");
        if (!ext.isEmpty() && base.toLowerCase().endsWith("." + ext)) {
            base = base.substring(0, base.length() - ext.length() - 1);
        }
        if (base.isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }
        return ext.isEmpty() ? base : base + "." + ext;
    }

    /** 生成 objectKey：myblog/{用户ID}/{业务类型}/yyyy/MM/{uuid}.{ext} */
    private String buildObjectKey(String bizType, String ext) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            // 登录上下文（JWT 过滤器）接入后自动使用当前登录用户
            userId = 1L;
        }
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        return ossProperties.getDirPrefix() + userId + "/" + bizType + "/" + datePath + "/"
                + UUID.randomUUID().toString().replace("-", "") + "." + ext;
    }

    /** 组装可访问 URL（优先自定义域名） */
    private String buildUrl(String objectKey) {
        String domain = ossProperties.getDomain();
        if (domain != null && !domain.isBlank()) {
            return domain.endsWith("/") ? domain + objectKey : domain + "/" + objectKey;
        }
        return "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/" + objectKey;
    }

    /** 提取扩展名（小写） */
    private String resolveExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}