package com.github.ecc1esia.picture.infrastructure.api.aliyunai;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.github.ecc1esia.picture.infrastructure.exception.BusinessException;
import com.github.ecc1esia.picture.infrastructure.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AliYunAiApi {
    // 读取配置文件
    @Value("{aliYunAi.apiKey}")
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
                .header("Authorization", "Bearer" + apiKey)
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
                .header("Authorization", "Bearer" + apiKey)
                .execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求异常:{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务结果失败");
            }
            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
        }
    }
}
