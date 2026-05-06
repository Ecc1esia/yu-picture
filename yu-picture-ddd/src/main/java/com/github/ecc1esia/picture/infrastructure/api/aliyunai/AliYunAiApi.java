package com.github.ecc1esia.picture.infrastructure.api.aliyunai;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.CreateTextToImageTaskRequest;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.CreateTextToImageTaskResponse;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.GetTextToImageTaskResponse;
import com.github.ecc1esia.picture.infrastructure.exception.BusinessException;
import com.github.ecc1esia.picture.infrastructure.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AliYunAiApi {
    // 读取配置文件
    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    // 创建任务地址
    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    // 查询任务状态
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";
    // 百炼多模态Embedding API地址
    public static final String MULTIMODAL_EMBEDDING_URL = "https://dashscope.aliyuncs.com/api/v1/services/embeddings/multimodal-embedding/multimodal-embedding";
    // 文生图
    public static final String TEXT_TO_IMAGE_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis";

    /**
     * 创建任务
     *
     * @param request
     * @return
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest request) {

        if (request == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "扩图参数为空");
        }

        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                .header("Authorization", "Bearer " + apiKey)
                // 异步
                .header("X-DashScope-Async", "true")
                .header("Content-type", "application/json")
                .body(JSONUtil.toJsonStr(request));


        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求异常:{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败");
            }

            CreateOutPaintingTaskResponse response = JSONUtil.toBean(httpResponse.body(), CreateOutPaintingTaskResponse.class);

            if (response.getCode() != null) {
                String errorMessage = response.getMessage();
                log.error("请求异常:{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败," + errorMessage);
            }
            return response;
        }
    }

    /**
     * 查询创建的任务结果
     *
     * @param taskId
     * @return
     */
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务ID不能为空");
        }

        // 处理响应
        String url = String.format(GET_OUT_PAINTING_TASK_URL, taskId);
        try (HttpResponse httpResponse = HttpRequest.get(url)
                .header("Authorization", "Bearer " + apiKey)
                .execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求异常:{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务结果失败");
            }
            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
        }
    }


    /**
     * 创建文生图任务
     */
    public CreateTextToImageTaskResponse createTextToImageTask(CreateTextToImageTaskRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文生图参数为空");
        }

        HttpRequest httpRequest = HttpRequest.post(TEXT_TO_IMAGE_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("X-DashScope-Async", "true")
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(request));

        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                log.error("文生图请求异常:{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "文生图请求失败");
            }

            CreateTextToImageTaskResponse response = JSONUtil.toBean(httpResponse.body(), CreateTextToImageTaskResponse.class);

            if (response.getCode() != null) {
                log.error("文生图请求异常:{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "文生图失败," + response.getMessage());
            }
            return response;
        }
    }

    /**
     * 查询文生图任务结果
     */
    public GetTextToImageTaskResponse getTextToImageTask(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务ID不能为空");
        }

        String url = String.format(GET_OUT_PAINTING_TASK_URL, taskId);
        try (HttpResponse httpResponse = HttpRequest.get(url)
                .header("Authorization", "Bearer " + apiKey)
                .execute()) {
            if (!httpResponse.isOk()) {
                log.error("查询文生图任务异常:{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "查询文生图任务失败");
            }
            return JSONUtil.toBean(httpResponse.body(), GetTextToImageTaskResponse.class);
        }
    }

    /**
     * 提取图片向量（使用百炼多模态Embedding API）
     *
     * @param imageUrl 图片URL（公网可访问）
     * @return float[] 向量数组
     */
    public float[] extractImageVector(String imageUrl) {
        return extractImageVector(imageUrl, false);
    }

    /**
     * 提取融合向量（文本+图像，使用百炼多模态Embedding API）
     *
     * @param imageUrl 图片URL（公网可访问）
     * @param text     文本内容（可为空）
     * @return float[] 融合向量数组
     */
    public float[] extractFusionVector(String imageUrl, String text) {
        return extractFusionVector(imageUrl, text, "qwen3-vl-embedding");
    }

    /**
     * 提取融合向量（文本+图像，使用百炼多模态Embedding API）
     *
     * @param imageUrl 图片URL（公网可访问）
     * @param text     文本内容（可为空）
     * @param model    模型名称
     * @return float[] 融合向量数组
     */
    public float[] extractFusionVector(String imageUrl, String text, String model) {
        if (StrUtil.isBlank(imageUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片URL不能为空");
        }

        // 构建请求体（按官方文档格式）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        // 构建 contents 数组
        List<Map<String, String>> contents = new ArrayList<>();

        // 添加文本（如果提供）
        if (StrUtil.isNotBlank(text)) {
            Map<String, String> textItem = new HashMap<>();
            textItem.put("text", text);
            contents.add(textItem);
        }

        // 添加图像
        Map<String, String> imageItem = new HashMap<>();
        imageItem.put("image", imageUrl);
        contents.add(imageItem);

        Map<String, Object> inputWrapper = new HashMap<>();
        inputWrapper.put("contents", contents);
        requestBody.put("input", inputWrapper);

        // 添加 parameters（融合向量必须 enable_fusion: true）
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("enable_fusion", true);
        requestBody.put("parameters", parameters);

        return doEmbeddingRequest(requestBody);
    }

    /**
     * 提取图片向量（使用百炼多模态Embedding API）
     *
     * @param imageUrl     图片URL（公网可访问）
     * @param enableFusion 是否启用融合向量
     * @return float[] 向量数组
     */
    public float[] extractImageVector(String imageUrl, boolean enableFusion) {
        if (StrUtil.isBlank(imageUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片URL不能为空");
        }

        // 构建请求体（按官方文档格式）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "qwen3-vl-embedding");

        // 构建 contents 数组
        List<Map<String, String>> contents = new ArrayList<>();
        Map<String, String> imageItem = new HashMap<>();
        imageItem.put("image", imageUrl);
        contents.add(imageItem);

        Map<String, Object> inputWrapper = new HashMap<>();
        inputWrapper.put("contents", contents);
        requestBody.put("input", inputWrapper);

        // 添加 parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("enable_fusion", enableFusion);
        requestBody.put("parameters", parameters);

        return doEmbeddingRequest(requestBody);
    }

    /**
     * 执行向量化请求的通用方法
     *
     * @param requestBody 请求体
     * @return float[] 向量数组
     */
    private float[] doEmbeddingRequest(Map<String, Object> requestBody) {
        try {
            HttpResponse httpResponse = HttpRequest.post(MULTIMODAL_EMBEDDING_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(requestBody))
                    .execute();

            if (!httpResponse.isOk()) {
                log.error("提取向量失败: {}", httpResponse.body());
                log.error("错误响应: {}", httpResponse);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "提取向量失败");
            }

            JSONObject jsonObject = JSONUtil.parseObj(httpResponse.body());
            JSONObject output = jsonObject.getJSONObject("output");
            if (output == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "提取向量返回数据格式错误");
            }

            // 融合向量格式：output.embedding (直接数组)
            if (output.containsKey("embedding")) {
                JSONArray embeddingArray = output.getJSONArray("embedding");
                float[] vector = new float[embeddingArray.size()];
                for (int i = 0; i < embeddingArray.size(); i++) {
                    vector[i] = embeddingArray.getFloat(i);
                }
                return vector;
            }

            // 标准格式：output.embeddings[0].embedding
            JSONArray embeddings = output.getJSONArray("embeddings");
            if (embeddings == null || embeddings.isEmpty()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "提取向量返回为空");
            }

            JSONObject firstEmbedding = embeddings.getJSONObject(0);
            JSONArray embeddingVector = firstEmbedding.getJSONArray("embedding");

            float[] vector = new float[embeddingVector.size()];
            for (int i = 0; i < embeddingVector.size(); i++) {
                vector[i] = embeddingVector.getFloat(i);
            }

            return vector;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("提取向量异常", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "提取向量异常: " + e.getMessage());
        }
    }
}
