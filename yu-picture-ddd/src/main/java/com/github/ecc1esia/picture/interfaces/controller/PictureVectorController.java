package com.github.ecc1esia.picture.interfaces.controller;

import com.github.ecc1esia.picture.domain.picture.entity.PictureVectorRecord;
import com.github.ecc1esia.picture.domain.picture.service.PictureVectorService;
import com.github.ecc1esia.picture.domain.user.entity.User;
import com.github.ecc1esia.picture.application.service.UserApplicationService;
import com.github.ecc1esia.picture.infrastructure.api.CosManager;
import com.github.ecc1esia.picture.infrastructure.common.BaseResponse;
import com.github.ecc1esia.picture.infrastructure.common.ResultUtils;
import com.github.ecc1esia.picture.infrastructure.exception.BusinessException;
import com.github.ecc1esia.picture.infrastructure.exception.ErrorCode;
import com.github.ecc1esia.picture.infrastructure.exception.ThrowUtils;
import com.github.ecc1esia.picture.interfaces.dto.picture.SearchVectorByPictureRequest;
import com.github.ecc1esia.picture.interfaces.vo.picture.VectorSearchResultVO;
import com.github.ecc1esia.picture.shared.auth.annotation.SaSpaceCheckPermission;
import com.github.ecc1esia.picture.shared.auth.model.SpaceUserPermissionConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 图片向量搜索控制器
 */
@Slf4j
@RestController
@RequestMapping("/picture/search/vector")
public class PictureVectorController {

    private static final int DEFAULT_TOP_K = 10;
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/jpg", "image/bmp", "image/webp"
    );

    @Resource
    private PictureVectorService pictureVectorService;

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片搜图
     */
    @PostMapping("/by-upload")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<VectorSearchResultVO>> searchByUpload(
            @RequestPart("file") MultipartFile file,
            @RequestParam("spaceId") Long spaceId,
            HttpServletRequest request) {

        // 参数校验
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "图片文件不能为空");
        ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR, "空间ID不能为空");

        // 校验文件类型
        String contentType = file.getContentType();
        ThrowUtils.throwIf(!ALLOWED_IMAGE_TYPES.contains(contentType),
                ErrorCode.PARAMS_ERROR, "只能上传图片文件");

        // 获取登录用户
        User loginUser = userApplicationService.getLoginUser(request);

        // 上传到COS获取公网URL
        String tempImageUrl = uploadToCosAndGetUrl(file);

        // 搜索相似图片
        List<PictureVectorRecord> results = pictureVectorService.searchSimilarPictures(
                spaceId, tempImageUrl, DEFAULT_TOP_K);

        // 转换结果
        List<VectorSearchResultVO> voList = results.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return ResultUtils.success(voList);
    }

    /**
     * 选择已有图片搜图
     */
    @PostMapping("/by-picture")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<VectorSearchResultVO>> searchByPicture(
            @RequestBody SearchVectorByPictureRequest request,
            HttpServletRequest httpRequest) {

        // 参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(request.getPictureId() == null, ErrorCode.PARAMS_ERROR, "图片ID不能为空");
        ThrowUtils.throwIf(request.getSpaceId() == null, ErrorCode.PARAMS_ERROR, "空间ID不能为空");

        // 获取登录用户
        User loginUser = userApplicationService.getLoginUser(httpRequest);

        // 搜索相似图片
        List<PictureVectorRecord> results = pictureVectorService.searchSimilarPicturesById(
                request.getSpaceId(), request.getPictureId(), DEFAULT_TOP_K);

        // 转换结果
        List<VectorSearchResultVO> voList = results.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return ResultUtils.success(voList);
    }

    /**
     * 转换为VO
     */
    private VectorSearchResultVO convertToVO(PictureVectorRecord record) {
        return VectorSearchResultVO.builder()
                .pictureId(record.getPictureId())
                .name(record.getName())
                .thumbnailUrl(record.getThumbnailUrl())
                .url(record.getUrl())
                .score(record.getScore())
                .build();
    }

    /**
     * 将上传文件保存到COS并返回公网访问URL
     */
    private String uploadToCosAndGetUrl(MultipartFile file) {
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String key = "temp/vector-search/" + UUID.randomUUID().toString() + extension;

            // 转换为File并上传
            File tempFile = File.createTempFile("upload-", extension);
            file.transferTo(tempFile);
            cosManager.putObject(key, tempFile);

            // 删除临时文件
            tempFile.delete();

            // 返回COS公网访问URL
            return cosManager.getPublicAccessUrl(key);
        } catch (IOException e) {
            log.error("上传文件到COS失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片上传失败");
        }
    }
}
