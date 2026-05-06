package com.github.ecc1esia.picture.interfaces.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建文生图任务请求（接口层 DTO）
 */
@Data
public class CreateTextToImageTaskRequest implements Serializable {

    /**
     * 文本提示词
     */
    private String prompt;

    /**
     * 图片尺寸，默认 "1024*1024"
     */
    private String size = "1024*1024";

    /**
     * 生成数量，默认 1
     */
    private Integer n = 1;

    /**
     * 风格，默认 "<auto>"
     */
    private String style = "<auto>";

    private static final long serialVersionUID = 1L;
}
