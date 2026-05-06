package com.github.ecc1esia.picture.interfaces.controller;

import cn.hutool.core.util.StrUtil;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.AliYunAiApi;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.CreateTextToImageTaskRequest;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.CreateTextToImageTaskResponse;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.GetTextToImageTaskResponse;
import com.github.ecc1esia.picture.infrastructure.common.BaseResponse;
import com.github.ecc1esia.picture.infrastructure.common.ResultUtils;
import com.github.ecc1esia.picture.infrastructure.exception.ErrorCode;
import com.github.ecc1esia.picture.infrastructure.exception.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/text2image")
public class TextToImageController {

    private static final Set<String> ALLOWED_SIZES = Set.of("1024*1024", "720*1280", "1280*720");
    private static final Set<String> ALLOWED_STYLES = Set.of(
            "<auto>", "<photography>", "<portrait>", "<3d-cartoon>",
            "<anime>", "<oil-painting>", "<watercolor>", "<sketch>", "<chinese-painting>");
    private static final int MAX_N = 4;

    @Resource
    private AliYunAiApi aliYunAiApi;

    /**
     * 创建文生图任务
     */
    @PostMapping("/create_task")
    public BaseResponse<CreateTextToImageTaskResponse> createTask(
            @RequestBody com.github.ecc1esia.picture.interfaces.dto.picture.CreateTextToImageTaskRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(request.getPrompt()), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        // 参数白名单校验
        if (StrUtil.isNotBlank(request.getSize())) {
            ThrowUtils.throwIf(!ALLOWED_SIZES.contains(request.getSize()), ErrorCode.PARAMS_ERROR, "不支持的图片尺寸");
        }
        if (StrUtil.isNotBlank(request.getStyle())) {
            ThrowUtils.throwIf(!ALLOWED_STYLES.contains(request.getStyle()), ErrorCode.PARAMS_ERROR, "不支持的风格");
        }
        ThrowUtils.throwIf(request.getN() != null && (request.getN() < 1 || request.getN() > MAX_N), ErrorCode.PARAMS_ERROR, "生成数量超出范围(1-4)");

        CreateTextToImageTaskRequest apiRequest = new CreateTextToImageTaskRequest();
        CreateTextToImageTaskRequest.Input input = new CreateTextToImageTaskRequest.Input();
        input.setPrompt(request.getPrompt());
        apiRequest.setInput(input);
        CreateTextToImageTaskRequest.Parameters parameters = new CreateTextToImageTaskRequest.Parameters();
        parameters.setSize(request.getSize() != null ? request.getSize() : "1024*1024");
        parameters.setN(request.getN() != null ? request.getN() : 1);
        parameters.setStyle(request.getStyle() != null ? request.getStyle() : "<auto>");
        apiRequest.setParameters(parameters);

        CreateTextToImageTaskResponse response = aliYunAiApi.createTextToImageTask(apiRequest);
        return ResultUtils.success(response);
    }

    /**
     * 查询文生图任务结果
     */
    @GetMapping("/get_task/{taskId}")
    public BaseResponse<GetTextToImageTaskResponse> getTask(@PathVariable String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR, "任务ID不能为空");

        GetTextToImageTaskResponse response = aliYunAiApi.getTextToImageTask(taskId);
        return ResultUtils.success(response);
    }
}
