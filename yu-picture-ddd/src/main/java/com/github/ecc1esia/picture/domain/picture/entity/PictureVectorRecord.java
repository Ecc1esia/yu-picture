package com.github.ecc1esia.picture.domain.picture.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 图片向量记录（ES 文档实体）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PictureVectorRecord {

    /**
     * 图片 ID
     */
    private Long pictureId;

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 原图 URL
     */
    private String url;

    /**
     * 缩略图 URL
     */
    private String thumbnailUrl;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 图片向量（512维，注意：维度需与阿里云百炼API返回一致）
     */
    private float[] vector;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 相似度分数（搜索时返回，非持久化字段）
     */
    private Double score;
}
