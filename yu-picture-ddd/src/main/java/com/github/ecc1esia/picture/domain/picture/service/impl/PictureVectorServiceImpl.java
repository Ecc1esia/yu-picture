package com.github.ecc1esia.picture.domain.picture.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.ecc1esia.picture.domain.picture.entity.Picture;
import com.github.ecc1esia.picture.domain.picture.entity.PictureVectorRecord;
import com.github.ecc1esia.picture.domain.picture.service.PictureVectorService;
import com.github.ecc1esia.picture.domain.picture.repository.PictureRepository;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.AliYunAiApi;
import com.github.ecc1esia.picture.infrastructure.exception.BusinessException;
import com.github.ecc1esia.picture.infrastructure.exception.ErrorCode;
import com.github.ecc1esia.picture.infrastructure.exception.ThrowUtils;
import com.github.ecc1esia.picture.infrastructure.repository.PictureVectorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图片向量服务实现
 */
@Slf4j
@Service
public class PictureVectorServiceImpl implements PictureVectorService {

    @Resource
    private AliYunAiApi aliYunAiApi;

    @Resource
    private PictureVectorRepository pictureVectorRepository;

    @Resource
    private PictureRepository pictureRepository;

    @Override
    public void savePictureVector(Picture picture) {
        if (picture == null || picture.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片信息不完整");
        }

        try {
            // 提取图片向量
            float[] vector = aliYunAiApi.extractImageVector(picture.getUrl());

            // 构建向量记录
            PictureVectorRecord record = PictureVectorRecord.builder()
                    .pictureId(picture.getId())
                    .spaceId(picture.getSpaceId())
                    .url(picture.getUrl())
                    .thumbnailUrl(picture.getThumbnailUrl())
                    .name(picture.getName())
                    .vector(vector)
                    .createTime(LocalDateTime.now())
                    .build();

            // 保存到ES
            pictureVectorRepository.save(record);
            log.info("图片向量保存成功, pictureId={}", picture.getId());

        } catch (Exception e) {
            log.error("保存图片向量失败, pictureId={}", picture.getId(), e);
            // 向量保存失败不影响主流程，仅记录日志
        }
    }

    @Override
    public List<PictureVectorRecord> searchSimilarPictures(Long spaceId, String imageUrl, int topK) {
        if (spaceId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间ID不能为空");
        }
        if (StrUtil.isBlank(imageUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片URL不能为空");
        }

        try {
            // 提取查询图片的向量
            float[] queryVector = aliYunAiApi.extractImageVector(imageUrl);

            // ES KNN搜索
            List<PictureVectorRecord> results = pictureVectorRepository.search(spaceId, queryVector, topK);

            // 过滤已删除的图片
            return filterDeletedPictures(results);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("向量搜索失败, spaceId={}", spaceId, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片搜索失败");
        }
    }

    @Override
    public List<PictureVectorRecord> searchSimilarPicturesById(Long spaceId, Long pictureId, int topK) {
        if (spaceId == null || pictureId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不完整");
        }

        // 查询源图片
        Picture sourcePicture = pictureRepository.getById(pictureId);
        ThrowUtils.throwIf(sourcePicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        // 校验图片是否属于指定空间
        if (!spaceId.equals(sourcePicture.getSpaceId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片不在指定空间中");
        }

        return searchSimilarPictures(spaceId, sourcePicture.getUrl(), topK);
    }

    @Override
    public void deletePictureVector(Long pictureId) {
        if (pictureId == null) {
            return;
        }
        pictureVectorRepository.deleteByPictureId(pictureId);
        log.info("图片向量删除成功, pictureId={}", pictureId);
    }

    /**
     * 过滤已删除的图片
     */
    private List<PictureVectorRecord> filterDeletedPictures(List<PictureVectorRecord> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        List<Long> pictureIds = records.stream()
                .map(PictureVectorRecord::getPictureId)
                .collect(Collectors.toList());

        // 批量查询图片是否存在
        List<Picture> pictures = pictureRepository.listByIds(pictureIds);

        // 转换为Set用于快速查找
        List<Long> existingIds = pictures.stream()
                .filter(p -> p.getIsDelete() == 0) // 未删除
                .map(Picture::getId)
                .collect(Collectors.toList());

        // 过滤
        return records.stream()
                .filter(r -> existingIds.contains(r.getPictureId()))
                .collect(Collectors.toList());
    }
}
