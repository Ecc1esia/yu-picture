package com.github.ecc1esia.picture.interfaces.controller;

import cn.hutool.core.util.StrUtil;
import com.github.ecc1esia.picture.domain.picture.entity.Picture;
import com.github.ecc1esia.picture.domain.picture.repository.PictureRepository;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.AliYunAiApi;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.CreateTextToImageTaskRequest;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.CreateTextToImageTaskResponse;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.GetTextToImageTaskResponse;
import com.github.ecc1esia.picture.infrastructure.common.BaseResponse;
import com.github.ecc1esia.picture.infrastructure.common.ResultUtils;
import com.github.ecc1esia.picture.infrastructure.exception.ErrorCode;
import com.github.ecc1esia.picture.infrastructure.exception.ThrowUtils;
import com.github.ecc1esia.picture.interfaces.dto.picture.CreateStyleTransferTaskRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/style-transfer")
public class StyleTransferController {

    private static final Set<String> ALLOWED_STYLES = Set.of(
            "<auto>", "<photography>", "<portrait>", "<3d-cartoon>",
            "<anime>", "<oil-painting>", "<watercolor>", "<sketch>", "<chinese-painting>");

    @Resource
    private AliYunAiApi aliYunAiApi;

    @Resource
    private PictureRepository pictureRepository;

    /**
     * 创建风格迁移任务（基于已有图片 + 目标风格）
     */
    @PostMapping("/create_task")
    public BaseResponse<CreateTextToImageTaskResponse> createTask(
            @RequestBody CreateStyleTransferTaskRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getPictureId() == null, ErrorCode.PARAMS_ERROR, "图片ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(request.getStyle()), ErrorCode.PARAMS_ERROR, "风格不能为空");
        ThrowUtils.throwIf(!ALLOWED_STYLES.contains(request.getStyle()), ErrorCode.PARAMS_ERROR, "不支持的风格");

        // 查询源图片
        Picture picture = pictureRepository.getById(request.getPictureId());
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        // 从图片元数据构建提示词
        StringBuilder prompt = new StringBuilder();
        if (StrUtil.isNotBlank(picture.getName())) {
            prompt.append(picture.getName());
        }
        if (StrUtil.isNotBlank(picture.getIntroduction())) {
            prompt.append("，").append(picture.getIntroduction());
        }
        if (StrUtil.isNotBlank(picture.getCategory())) {
            prompt.append("，分类：").append(picture.getCategory());
        }
        // 以目标风格重新绘制
        String stylePrompt = "以" + request.getStyle() + "风格重新绘制：" + prompt;

        // 调用文生图 API
        CreateTextToImageTaskRequest apiRequest = new CreateTextToImageTaskRequest();
        CreateTextToImageTaskRequest.Input input = new CreateTextToImageTaskRequest.Input();
        input.setPrompt(stylePrompt);
        apiRequest.setInput(input);
        CreateTextToImageTaskRequest.Parameters parameters = new CreateTextToImageTaskRequest.Parameters();
        parameters.setStyle(request.getStyle());
        parameters.setN(1);
        parameters.setSize("1024*1024");
        apiRequest.setParameters(parameters);

        CreateTextToImageTaskResponse response = aliYunAiApi.createTextToImageTask(apiRequest);
        return ResultUtils.success(response);
    }

    /**
     * 查询风格迁移任务结果
     */
    @GetMapping("/get_task/{taskId}")
    public BaseResponse<GetTextToImageTaskResponse> getTask(@PathVariable String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        GetTextToImageTaskResponse response = aliYunAiApi.getTextToImageTask(taskId);
        return ResultUtils.success(response);
    }
}
