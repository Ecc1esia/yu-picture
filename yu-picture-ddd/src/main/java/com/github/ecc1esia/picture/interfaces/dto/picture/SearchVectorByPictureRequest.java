package com.github.ecc1esia.picture.interfaces.dto.picture;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 按已有图片搜索向量请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchVectorByPictureRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 已有的图片ID
     */
    private Long pictureId;

    /**
     * 目标空间ID
     */
    private Long spaceId;
}
