package com.github.ecc1esia.backend.controller;

import com.github.ecc1esia.backend.annotation.AuthCheck;
import com.github.ecc1esia.backend.common.BaseResponse;
import com.github.ecc1esia.backend.common.DeleteRequest;
import com.github.ecc1esia.backend.common.ResultUtils;
import com.github.ecc1esia.backend.constant.UserConstant;
import com.github.ecc1esia.backend.exception.BusinessException;
import com.github.ecc1esia.backend.exception.ErrorCode;
import com.github.ecc1esia.backend.exception.ThrowUtils;
import com.github.ecc1esia.backend.manager.auth.SpaceUserAuthManager;
import com.github.ecc1esia.backend.model.dto.space.SpaceAddRequest;
import com.github.ecc1esia.backend.model.dto.space.SpaceEditRequest;
import com.github.ecc1esia.backend.model.dto.space.SpaceLevel;
import com.github.ecc1esia.backend.model.dto.space.SpaceUpdateRequest;
import com.github.ecc1esia.backend.model.entity.Space;
import com.github.ecc1esia.backend.model.entity.User;
import com.github.ecc1esia.backend.model.enums.SpaceLevelEnum;
import com.github.ecc1esia.backend.service.SpaceService;
import com.github.ecc1esia.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 空间控制器，用于处理与空间相关的请求
 */
@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 添加空间
     *
     * @param spaceAddRequest 空间添加请求对象
     * @param request         HTTP请求对象，用于获取登录用户信息
     * @return 返回添加成功后的空间ID
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long newId = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(newId);
    }

    /**
     * 删除空间
     *
     * @param deleteRequest 删除请求对象，包含要删除的空间ID
     * @param request       HTTP请求对象，用于获取登录用户信息
     * @return 返回删除操作的结果
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(
            @RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(request);
        Long id = deleteRequest.getId();

        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);

        spaceService.checkSpaceAuth(loginUser, oldSpace);
        boolean result = spaceService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新空间（仅管理员可用）
     *
     * @param spaceUpdateRequest 空间更新请求对象
     * @param request            HTTP请求对象，用于获取登录用户信息
     * @return 返回更新操作的结果
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(
            @RequestBody SpaceUpdateRequest spaceUpdateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 将实体类转换为 DTO
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        spaceService.fillSpaceBySpaceLevel(space);
        spaceService.validSpace(space, false);

        long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);

        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = spaceService.updateById(space);

        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取空间（仅管理员可用）
     *
     * @param id      空间ID
     * @param request HTTP请求对象，用于获取登录用户信息
     * @return 返回查询到的空间对象
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(space);
    }

    /**
     * 编辑空间信息
     *
     * @param spaceEditRequest 空间编辑请求对象
     * @param request          HTTP请求对象，用于获取登录用户信息
     * @return 返回编辑操作的结果
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(
            @RequestBody SpaceEditRequest spaceEditRequest,
            HttpServletRequest request) {

        if (spaceEditRequest == null || spaceEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);

        spaceService.fillSpaceBySpaceLevel(space);
        space.setEditTime(new Date());

        spaceService.validSpace(space, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = spaceEditRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        spaceService.checkSpaceAuth(loginUser, oldSpace);
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return ResultUtils.success(true);
    }

    /**
     * 获取空间级别列表，便于前端展示
     *
     * @return 返回空间级别列表
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()
                )).collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }
}
