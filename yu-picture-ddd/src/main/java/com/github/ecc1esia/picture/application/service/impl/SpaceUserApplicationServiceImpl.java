package com.github.ecc1esia.picture.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.ecc1esia.picture.application.service.SpaceApplicationService;
import com.github.ecc1esia.picture.application.service.SpaceUserApplicationService;
import com.github.ecc1esia.picture.application.service.UserApplicationService;
import com.github.ecc1esia.picture.domain.space.entity.Space;
import com.github.ecc1esia.picture.domain.space.entity.SpaceUser;
import com.github.ecc1esia.picture.domain.space.service.SpaceUserDomainService;
import com.github.ecc1esia.picture.domain.space.valueobject.SpaceRoleEnum;
import com.github.ecc1esia.picture.domain.user.entity.User;
import com.github.ecc1esia.picture.infrastructure.exception.ErrorCode;
import com.github.ecc1esia.picture.infrastructure.exception.ThrowUtils;
import com.github.ecc1esia.picture.infrastructure.mapper.SpaceUserMapper;
import com.github.ecc1esia.picture.interfaces.assembler.SpaceUserAssembler;
import com.github.ecc1esia.picture.interfaces.dto.spaceuser.SpaceUserAddRequest;
import com.github.ecc1esia.picture.interfaces.dto.spaceuser.SpaceUserQueryRequest;
import com.github.ecc1esia.picture.interfaces.vo.space.SpaceUserVO;
import com.github.ecc1esia.picture.interfaces.vo.space.SpaceVO;
import com.github.ecc1esia.picture.interfaces.vo.user.UserVO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 针对表【space_user(空间用户关联)】的数据库操作Service实现
 * todo
 */
@Service
public class SpaceUserApplicationServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
        implements SpaceUserApplicationService {
    @Resource
    private SpaceUserDomainService spaceUserDomainService;

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    @Lazy
    private SpaceApplicationService spaceApplicationService;

    /**
     * @param spaceUserAddRequest
     * @return
     */
    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        // 参数校验
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = SpaceUserAssembler.toSpaceUserEntity(spaceUserAddRequest);
        validSpaceUser(spaceUser, true);
        // 数据库操作
        boolean result = this.save(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return spaceUser.getId();
    }

    /**
     * @param spaceUser
     * @param add       是否为创建时检验
     */
    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR);
        // 创建时，空间id 和 用户 id必填
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        if (add) {
            ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
            User user = userApplicationService.getUserById(userId);
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            Space space = spaceApplicationService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        // 校验空间角色
        String spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        ThrowUtils.throwIf(spaceRole == null && spaceRoleEnum == null, ErrorCode.PARAMS_ERROR, "空间角色不存在");
    }

    /**
     * @param spaceUser
     * @param request
     * @return
     */
    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) {
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
        // 关联查询用户信息
        Long userId = spaceUser.getUserId();
        if (userId != null && userId > 0) {
            User user = userApplicationService.getUserById(userId);
            UserVO userVO = userApplicationService.getUserVO(user);
            spaceUserVO.setUser(userVO);
        }
        // 关联查询空间信息
        Long spaceId = spaceUser.getSpaceId();
        if (spaceId != null && spaceId > 0) {
            Space space = spaceApplicationService.getById(spaceId);
            SpaceVO spaceVO = spaceApplicationService.getSpaceVO(space, request);
            spaceUserVO.setSpace(spaceVO);
        }
        return spaceUserVO;
    }

    /**
     * @param spaceUserList
     * @return
     */
    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        // 判断输入列表是否为空
        if (CollUtil.isEmpty(spaceUserList)) {
            return Collections.emptyList();
        }
        // 对象列表 => 封装对象列表
        List<SpaceUserVO> spaceUserVOList = spaceUserList.stream().map(SpaceUserVO::objToVo).collect(Collectors.toList());
        // 1. 搜集需要关联查询用户的 ID 和 空间 ID
        Set<Long> userIdSet = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        Set<Long> spaceIdSet = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());
        // 2. 批量关联查询用户信息
        Map<Long, List<User>> userIdUserListMap = userApplicationService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        Map<Long, List<Space>> spaceIdSpaceListMap = spaceApplicationService.listByIds(spaceIdSet).stream()
                .collect(Collectors.groupingBy(Space::getId));
        // 3. 填充 SpaceUserVO 的用户信息和空间信息
        spaceUserVOList.forEach(spaceUserVO -> {
            Long userId = spaceUserVO.getUserId();
            Long spaceId = spaceUserVO.getSpaceId();
            // 填充用户信息
            User user = null;

            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceUserVO.setUser(userApplicationService.getUserVO(user));
            // 填充空间信息
            Space space = null;
            if (spaceIdSpaceListMap.containsKey(spaceId)) {
                space = spaceIdSpaceListMap.get(spaceId).get(0);
            }
            spaceUserVO.setSpace(SpaceVO.objToVo(space));
        });
        return spaceUserVOList;
    }

    /**
     * @param spaceUserQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        return spaceUserDomainService.getQueryWrapper(spaceUserQueryRequest);
    }
}

