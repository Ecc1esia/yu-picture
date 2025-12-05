package com.github.ecc1esia.picture.domain.space.service.impl;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.ecc1esia.picture.domain.space.entity.SpaceUser;
import com.github.ecc1esia.picture.domain.space.repository.SpaceUserRepository;
import com.github.ecc1esia.picture.domain.space.service.SpaceUserDomainService;
import com.github.ecc1esia.picture.interfaces.dto.spaceuser.SpaceUserQueryRequest;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

/**
 * 实现SpaceUserDomainService接口的服务类
 * 提供与SpaceUser相关的领域逻辑实现
 */
@Service
public class SpaceUserDomainServiceImpl implements SpaceUserDomainService {

    @Resource
    private SpaceUserRepository spaceUserRepository;

    /**
     * 根据查询请求生成SpaceUser的查询包装器
     * 此方法用于根据传入的查询请求参数，构造一个MyBatis-Plus的QueryWrapper对象，
     * 该对象用于执行后续的数据库查询操作
     *
     * @param spaceUserQueryRequest 查询请求对象，包含查询条件
     * @return 返回构造好的QueryWrapper对象
     */
    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        // 初始化一个空的查询包装器
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        // 如果查询请求为空，则直接返回空的查询包装器
        if (spaceUserQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();
        // 根据查询条件构建查询包装器
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceRole), "spaceRole", spaceRole);
        // 返回构建好的查询包装器
        return queryWrapper;
    }
}
