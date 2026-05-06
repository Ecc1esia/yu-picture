package com.github.ecc1esia.picture.domain.picture.repository;

import com.github.ecc1esia.picture.domain.picture.entity.PictureVectorRecord;

import java.util.List;

/**
 * 图片向量仓储接口
 */
public interface PictureVectorRepository {

    /**
     * 创建索引（启动时调用）
     */
    public void createIndexIfNotExists();

    /**
     * 保存图片向量
     *
     * @param record 向量记录
     */
    public void save(PictureVectorRecord record);

    /**
     * KNN 搜索
     *
     * @param spaceId     空间 ID
     * @param queryVector 查询向量
     * @param topK        返回数量
     * @return 匹配的记录列表
     */
    public List<PictureVectorRecord> search(Long spaceId, float[] queryVector, int topK);

    /**
     * 根据图片ID删除向量
     *
     * @param pictureId 图片 ID
     */
    public void deleteByPictureId(Long pictureId);
}
