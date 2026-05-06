package com.github.ecc1esia.picture.infrastructure.api.aliyunai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 查询文生图任务响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTextToImageTaskResponse {

    private String requestId;
    private Output output;

    @Data
    public static class Output {
        private String taskId;
        private String taskStatus;
        private String submitTime;
        private String scheduledTime;
        private String endTime;
        private List<Result> results;
        private String code;
        private String message;
        private TaskMetrics taskMetrics;
    }

    @Data
    public static class Result {
        private String url;
    }

    @Data
    public static class TaskMetrics {
        private Integer total;
        private Integer succeeded;
        private Integer failed;
    }
}
