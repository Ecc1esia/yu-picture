package com.github.ecc1esia.picture.infrastructure.api.aliyunai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建文生图任务响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTextToImageTaskResponse {

    private Output output;

    @Data
    public static class Output {
        private String taskId;
        private String taskStatus;
    }

    private String code;
    private String message;
    private String requestId;
}
