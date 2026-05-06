package com.github.ecc1esia.picture.infrastructure.api.aliyunai.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建文生图任务请求
 */
@Data
public class CreateTextToImageTaskRequest implements Serializable {

    /**
     * 模型，例如 "wanx2.1-t2i-turbo"
     */
    private String model = "wanx2.1-t2i-turbo";

    /**
     * 输入信息
     */
    private Input input;

    /**
     * 生成参数
     */
    private Parameters parameters;

    @Data
    public static class Input {
        /**
         * 必选，文本提示词
         */
        private String prompt;
    }

    @Data
    public static class Parameters implements Serializable {
        /**
         * 可选，图片尺寸，默认 "1024*1024"
         * 可选值：["1024*1024", "720*1280", "1280*720"]
         */
        private String size = "1024*1024";

        /**
         * 可选，生成数量，默认 1，最大 4
         */
        private Integer n = 1;

        /**
         * 可选，风格，默认 "<auto>"
         * 可选值：["<auto>", "<photography>", "<portrait>", "<3d-cartoon>", "<anime>", "<oil-painting>", "<watercolor>", "<sketch>", "<chinese-painting>"]
         */
        private String style = "<auto>";
    }
}
