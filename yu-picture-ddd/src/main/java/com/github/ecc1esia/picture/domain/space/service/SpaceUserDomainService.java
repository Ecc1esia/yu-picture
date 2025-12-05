package com.github.ecc1esia.picture.domain.space.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.ecc1esia.picture.domain.space.entity.SpaceUser;
import com.github.ecc1esia.picture.interfaces.dto.spaceuser.SpaceUserQueryRequest;

/**
 * 空间用户关联(SpaceUser)表服务接口
 *
 * @author jieyuu
 * @since 2025-04-24 15:33:24
 */
public interface SpaceUserDomainService {

    /**
     * 获取查询对象
     *
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);
}
