package com.github.ecc1esia.picture.domain.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.ecc1esia.picture.domain.user.entity.User;
import com.github.ecc1esia.picture.infrastructure.dao.UserDao;
import com.github.ecc1esia.picture.domain.user.service.UserDomainService;
import com.github.ecc1esia.picture.interfaces.dto.user.UserQueryRequest;
import com.github.ecc1esia.picture.interfaces.vo.user.LoginUserVO;
import com.github.ecc1esia.picture.interfaces.vo.user.UserVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * 用户(User)表服务实现类
 *
 * @author makejava
 * @since 2025-04-24 15:33:24
 */
@Service("userService")
public class UserDomainServiceImpl implements UserDomainService {
    @Resource
    private UserDao userDao;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        return 0;
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        return null;
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        return null;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        return null;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        return null;
    }

    @Override
    public UserVO getUserVO(User user) {
        return null;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        return null;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        return false;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        return null;
    }

    @Override
    public Boolean removeById(Long id) {
        return null;
    }

    @Override
    public boolean updateById(User user) {
        return false;
    }

    @Override
    public User getById(long id) {
        return null;
    }

    @Override
    public Page<User> page(Page<User> userPage, QueryWrapper<User> queryWrapper) {
        return null;
    }

    @Override
    public List<User> listByIds(Set<Long> userIdSet) {
        return null;
    }

    @Override
    public boolean saveUser(User userEntity) {
        return false;
    }
}
