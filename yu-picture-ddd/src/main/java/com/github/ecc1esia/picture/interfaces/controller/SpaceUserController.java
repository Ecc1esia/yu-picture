package com.github.ecc1esia.picture.interfaces.controller;

import com.github.ecc1esia.picture.application.service.SpaceUserApplicationService;
import com.github.ecc1esia.picture.application.service.UserApplicationService;
import com.github.ecc1esia.picture.domain.space.entity.SpaceUser;
import com.github.ecc1esia.picture.domain.user.entity.User;
import com.github.ecc1esia.picture.infrastructure.common.BaseResponse;
import com.github.ecc1esia.picture.infrastructure.common.DeleteRequest;
import com.github.ecc1esia.picture.infrastructure.common.ResultUtils;
import com.github.ecc1esia.picture.infrastructure.exception.ErrorCode;
import com.github.ecc1esia.picture.infrastructure.exception.ThrowUtils;
import com.github.ecc1esia.picture.interfaces.assembler.SpaceUserAssembler;
import com.github.ecc1esia.picture.interfaces.dto.spaceuser.SpaceUserAddRequest;
import com.github.ecc1esia.picture.interfaces.dto.spaceuser.SpaceUserEditRequest;
import com.github.ecc1esia.picture.interfaces.dto.spaceuser.SpaceUserQueryRequest;
import com.github.ecc1esia.picture.interfaces.vo.space.SpaceUserVO;
import com.github.ecc1esia.picture.shared.auth.annotation.SaSpaceCheckPermission;
import com.github.ecc1esia.picture.shared.auth.model.SpaceUserPermissionConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 空间用户关联(SpaceUser)表控制层
 * todo
 *
 * @author makejava
 * @since 2025-04-24 15:33:24
 */
@Slf4j
@RestController
@RequestMapping("spaceUser")
public class SpaceUserController {
    /**
     * 服务对象
     */
    @Resource
    private SpaceUserApplicationService spaceUserApplicationService;
    @Resource
    private UserApplicationService userApplicationService;

    /**
     * 添加成员到空间
     */
    @PostMapping("/add")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        long id = spaceUserApplicationService.addSpaceUser(spaceUserAddRequest);
        return ResultUtils.success(id);
    }

    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        long id = deleteRequest.getId();
        // 判断是否存在
        SpaceUser oldSpaceUser = spaceUserApplicationService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 数据库更新
        boolean result = spaceUserApplicationService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/get")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        // 校验
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        ThrowUtils.throwIf(spaceId == null || userId == null, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        SpaceUser spaceUser = spaceUserApplicationService.getOne(spaceUserApplicationService.getQueryWrapper(spaceUserQueryRequest));
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceUser);
    }

    /**
     * 查询成员信息列表
     */
    @PostMapping("/list")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest,
                                                         HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        List<SpaceUser> spaceUserList = spaceUserApplicationService.list(
                spaceUserApplicationService.getQueryWrapper(spaceUserQueryRequest)
        );
        return ResultUtils.success(spaceUserApplicationService.getSpaceUserVOList(spaceUserList));
    }

    /**
     * 编辑成员信息(权限设置)
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest, HttpServletRequest request) {

        ThrowUtils.throwIf(spaceUserEditRequest == null || spaceUserEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        SpaceUser spaceUser = SpaceUserAssembler.toSpaceUserEntity(spaceUserEditRequest);
        spaceUserApplicationService.validSpaceUser(spaceUser, true);
        // 判断是否存在
        long id = spaceUserEditRequest.getId();
        SpaceUser oldSpaceUser = spaceUserApplicationService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = spaceUserApplicationService.updateById(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 查询我加入的团队空间列表
     */
    @PostMapping("/list/my")
    public BaseResponse<List<SpaceUserVO>> listMyTeamSpace(HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());
        List<SpaceUser> spaceUserList = spaceUserApplicationService.list(
                spaceUserApplicationService.getQueryWrapper(spaceUserQueryRequest)
        );
        return ResultUtils.success(spaceUserApplicationService.getSpaceUserVOList(spaceUserList));
    }
}

