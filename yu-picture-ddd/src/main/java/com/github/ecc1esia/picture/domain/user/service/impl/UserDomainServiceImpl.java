package com.github.ecc1esia.picture.domain.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.ecc1esia.picture.domain.user.entity.User;
import com.github.ecc1esia.picture.domain.user.repository.UserRepository;
import com.github.ecc1esia.picture.infrastructure.exception.BusinessException;
import com.github.ecc1esia.picture.infrastructure.exception.ErrorCode;
import com.github.ecc1esia.picture.domain.user.service.UserDomainService;
import com.github.ecc1esia.picture.domain.user.valueobject.UserRoleEnum;
import com.github.ecc1esia.picture.interfaces.dto.user.UserQueryRequest;
import com.github.ecc1esia.picture.interfaces.vo.user.LoginUserVO;
import com.github.ecc1esia.picture.interfaces.vo.user.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * 用户(User)表服务实现类
 * todo
 * 
 * @author ecc1esia
 * @since 2025-04-24 15:33:24
 */
@Service("userService")
public class UserDomainServiceImpl implements UserDomainService {

    @Resource
    private UserRepository userRepository;

    UserDomainServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 检查用户账号是否和数据库中已有的重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userRepository.getBaseMapper().selectCount(queryWrapper);

        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        // 3. 密码一定要加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据到数据库中
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = userRepository.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
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
