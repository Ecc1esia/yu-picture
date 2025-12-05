package com.github.ecc1esia.backend.controller;

import com.github.ecc1esia.backend.common.BaseResponse;
import com.github.ecc1esia.backend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 主控制器，处理根路径的请求
 */
@RestController
@RequestMapping("/")
public class MainController {

    /**
     * 健康检查接口
     *
     * 此方法用于响应健康检查请求，返回一个表示系统状态的响应
     * 主要用于系统可用性的监控，例如在Docker容器或云服务中作为健康检查的端点
     *
     * @return BaseResponse<String> 返回一个包含状态信息的响应对象
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }

}
