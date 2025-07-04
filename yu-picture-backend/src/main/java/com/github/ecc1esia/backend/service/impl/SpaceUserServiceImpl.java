package com.github.ecc1esia.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.ecc1esia.backend.mapper.SpaceUserMapper;
import com.github.ecc1esia.backend.model.dto.spaceuser.SpaceUserAddRequest;
import com.github.ecc1esia.backend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.github.ecc1esia.backend.model.entity.SpaceUser;
import com.github.ecc1esia.backend.model.vo.SpaceUserVO;
import com.github.ecc1esia.backend.service.SpaceService;
import com.github.ecc1esia.backend.service.SpaceUserService;
import com.github.ecc1esia.backend.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * todo
 */
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
        implements SpaceUserService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private SpaceService spaceService;

    /**
     * @param spaceUserAddRequest
     * @return
     */
    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        return 0;
    }

    /**
     * @param spaceUser
     * @param add       是否为创建时检验
     */
    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {

    }

    /**
     * @param spaceUser
     * @param request
     * @return
     */
    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) {
        return null;
    }

    /**
     * @param spaceUserList
     * @return
     */
    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        return null;
    }

    /**
     * @param spaceUserQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        return null;
    }
}