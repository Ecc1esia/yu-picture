package com.github.ecc1esia.picture.domain.picture.service;

import com.github.ecc1esia.picture.domain.picture.entity.Picture;
import com.github.ecc1esia.picture.domain.picture.entity.PictureVectorRecord;

import java.util.List;

/**
 * 图片向量服务接口
 */
public interface PictureVectorService {

    /**
     * 保存图片向量到ES
     * @param picture 图片实体
     * @return 是否保存成功
     */
    boolean savePictureVector(Picture picture);

    /**
     * 通过图片URL提取向量并搜索相似图片
     * @param spaceId 空间ID
     * @param imageUrl 图片URL
     * @param topK 返回数量
     * @return 相似图片列表
     */
    List<PictureVectorRecord> searchSimilarPictures(Long spaceId, String imageUrl, int topK);

    /**
     * 通过已有图片ID搜索相似图片
     * @param spaceId 空间ID
     * @param pictureId 已有图片ID
     * @param topK 返回数量
     * @return 相似图片列表
     */
    List<PictureVectorRecord> searchSimilarPicturesById(Long spaceId, Long pictureId, int topK);

    /**
     * 重新索引空间下所有图片的向量
     * @param spaceId 空间ID
     */
    void reindexAllPictures(Long spaceId);

    /**
     * 删除图片向量
     * @param pictureId 图片ID
     */
    void deletePictureVector(Long pictureId);
}
