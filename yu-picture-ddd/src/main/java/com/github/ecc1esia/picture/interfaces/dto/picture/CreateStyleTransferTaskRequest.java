package com.github.ecc1esia.picture.interfaces.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建风格迁移任务请求
 */
@Data
public class CreateStyleTransferTaskRequest implements Serializable {

    /**
     * 图片 ID
     */
    private Long pictureId;

    /**
     * 目标风格
     */
    private String style;

    private static final long serialVersionUID = 1L;
}
