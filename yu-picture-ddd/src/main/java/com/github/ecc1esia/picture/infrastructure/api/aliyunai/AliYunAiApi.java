package com.github.ecc1esia.picture.infrastructure.api.aliyunai;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.GetOutPaintingTaskResponse;
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

    // 百炼多模态Embedding API地址
    public static final String MULTIMODAL_EMBEDDING_URL = "https://dashscope.aliyuncs.com/api/v1/services/embeddings/multimodal/embedding";

    /**
     * 提取图片向量（使用百炼多模态Embedding API）
     * @param imageUrl 图片URL（公网可访问）
     * @return float[] 向量数组
     */
    public float[] extractImageVector(String imageUrl) {
        if (StrUtil.isBlank(imageUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片URL不能为空");
        }

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "qwen-vl-embedding"); // 多模态embedding模型

        List<Map<String, String>> input = new ArrayList<>();
        Map<String, String> imageItem = new HashMap<>();
        imageItem.put("image", imageUrl);
        input.add(imageItem);

        Map<String, Object> inputWrapper = new HashMap<>();
        inputWrapper.put("input", input);
        requestBody.put("input", inputWrapper);

        try {
            HttpResponse httpResponse = HttpRequest.post(MULTIMODAL_EMBEDDING_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(requestBody))
                    .execute();

            if (!httpResponse.isOk()) {
                log.error("提取图片向量失败: {}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "提取图片向量失败");
            }

            JSONObject jsonObject = JSONUtil.parseObj(httpResponse.body());
            // 解析返回的向量数据（具体格式需参考阿里云百炼API文档调整）
            JSONObject data = jsonObject.getJSONObject("data");
            if (data == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "提取图片向量返回数据格式错误");
            }

            JSONArray embeddings = data.getJSONArray("embeddings");
            if (embeddings == null || embeddings.isEmpty()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "提取图片向量返回为空");
            }

            JSONObject firstEmbedding = embeddings.getJSONObject(0);
            JSONArray embeddingVector = firstEmbedding.getJSONArray("embedding");

            float[] vector = new float[embeddingVector.size()];
            for (int i = 0; i < embeddingVector.size(); i++) {
                vector[i] = embeddingVector.getFloat(i);
            }

            log.info("成功提取图片向量, imageUrl={}, dimension={}", imageUrl, vector.length);
            return vector;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("提取图片向量异常, imageUrl={}", imageUrl, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "提取图片向量异常: " + e.getMessage());
        }
    }
}
