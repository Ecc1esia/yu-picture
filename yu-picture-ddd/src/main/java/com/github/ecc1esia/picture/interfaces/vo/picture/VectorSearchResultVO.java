package com.github.ecc1esia.picture.interfaces.vo.picture;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 向量搜索结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorSearchResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图片ID
     */
    private Long pictureId;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 原图URL
     */
    private String url;

    /**
     * 相似度分值 (0-1)
     */
    private Double score;
}
