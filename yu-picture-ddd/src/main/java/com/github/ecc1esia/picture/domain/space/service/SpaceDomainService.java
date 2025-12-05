package com.github.ecc1esia.picture.domain.space.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.ecc1esia.picture.domain.space.entity.Space;
import com.github.ecc1esia.picture.domain.user.entity.User;
import com.github.ecc1esia.picture.interfaces.dto.space.SpaceQueryRequest;

/**
 * 空间(Space)表服务接口
 *
 * @author ecc1esia
 * @since 2025-04-24 15:33:24
 */
public interface SpaceDomainService {

    /**
     * 获取查询对象
     *
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 根据空间级别填充空间对象
     *
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 校验空间权限
     *
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser, Space space);
}
