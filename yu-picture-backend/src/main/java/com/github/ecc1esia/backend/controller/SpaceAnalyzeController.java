package com.github.ecc1esia.backend.controller;

import com.github.ecc1esia.backend.common.BaseResponse;
import com.github.ecc1esia.backend.common.ResultUtils;
import com.github.ecc1esia.backend.exception.ErrorCode;
import com.github.ecc1esia.backend.exception.ThrowUtils;
import com.github.ecc1esia.backend.model.dto.space.analyze.*;
import com.github.ecc1esia.backend.model.entity.Space;
import com.github.ecc1esia.backend.model.entity.User;
import com.github.ecc1esia.backend.model.vo.space.analyze.*;
import com.github.ecc1esia.backend.service.SpaceAnalyzeService;
import com.github.ecc1esia.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 空间分析控制器，用于处理与空间分析相关的请求
 */
@Slf4j
@RestController
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;

    /**
     * 获取空间的使用状态
     *
     * @param spaceUsageAnalyzeRequest 请求参数，包含获取空间使用状态所需的信息
     * @param request                  HTTP请求，用于获取登录用户信息
     * @return 返回空间使用状态的响应
     */
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(
            @RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,
            HttpServletRequest request
    ) {
        // 参数校验
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 获取空间使用状态分析结果
        SpaceUsageAnalyzeResponse spaceUsageAnalyze = spaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser);
        // 返回成功响应
        return ResultUtils.success(spaceUsageAnalyze);
    }

    /**
     * 获取空间图片分类分析
     *
     * @param spaceCategoryAnalyzeRequest 请求参数，包含获取空间图片分类分析所需的信息
     * @param request                     HTTP请求，用于获取登录用户信息
     * @return 返回空间图片分类分析结果的响应
     */
    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(
            @RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
            HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 获取空间图片分类分析结果
        List<SpaceCategoryAnalyzeResponse> spaceCategoryAnalyze = spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser);
        // 返回成功响应
        return ResultUtils.success(spaceCategoryAnalyze);
    }

    /**
     * 获取空间图片标签分析
     *
     * @param spaceTagAnalyzeRequest 请求参数，包含获取空间图片标签分析所需的信息
     * @param request                HTTP请求，用于获取登录用户信息
     * @return 返回空间图片标签分析结果的响应
     */
    @PostMapping("/tag")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(
            @RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest,
            HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 获取空间图片标签分析结果
        List<SpaceTagAnalyzeResponse> spaceTagAnalyze = spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
        // 返回成功响应
        return ResultUtils.success(spaceTagAnalyze);
    }

    /**
     * 获取空间图片大小分析
     *
     * @param spaceSizeAnalyzeRequest 请求参数，包含获取空间图片大小分析所需的信息
     * @param request                 HTTP请求，用于获取登录用户信息
     * @return 返回空间图片大小分析结果的响应
     */
    @PostMapping("/size")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(
            @RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest,
            HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 获取空间图片大小分析结果
        List<SpaceSizeAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
        // 返回成功响应
        return ResultUtils.success(resultList);
    }

    /**
     * 获取空间用户行为分析
     *
     * @param spaceUserAnalyzeRequest 请求参数，包含获取空间用户行为分析所需的信息
     * @param request                 HTTP请求，用于获取登录用户信息
     * @return 返回空间用户行为分析结果的响应
     */
    @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(
            @RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest,
            HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 获取空间用户行为分析结果
        List<SpaceUserAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
        // 返回成功响应
        return ResultUtils.success(resultList);
    }

    /**
     * 获取空间使用排行分析
     *
     * @param spaceRankAnalyzeRequest 请求参数，包含获取空间使用排行分析所需的信息
     * @param request                 HTTP请求，用于获取登录用户信息
     * @return 返回空间使用排行分析结果的响应
     */
    @PostMapping("/rank")
    public BaseResponse<List<Space>> getSpaceRankAnalyze(
            @RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest,
            HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 获取空间使用排行分析结果
        List<Space> resultList = spaceAnalyzeService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUser);
        // 返回成功响应
        return ResultUtils.success(resultList);
    }
}
