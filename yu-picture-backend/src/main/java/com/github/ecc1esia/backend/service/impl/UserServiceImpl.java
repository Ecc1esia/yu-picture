package com.github.ecc1esia.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.ecc1esia.backend.constant.UserConstant;
import com.github.ecc1esia.backend.exception.BusinessException;
import com.github.ecc1esia.backend.exception.ErrorCode;
import com.github.ecc1esia.backend.manager.auth.StpKit;
import com.github.ecc1esia.backend.model.dto.user.UserQueryRequest;
import com.github.ecc1esia.backend.model.dto.user.VipCode;
import com.github.ecc1esia.backend.model.entity.User;
import com.github.ecc1esia.backend.model.enums.UserRoleEnum;
import com.github.ecc1esia.backend.model.vo.LoginUserVO;
import com.github.ecc1esia.backend.model.vo.UserVO;
import com.github.ecc1esia.backend.service.UserService;
import com.github.ecc1esia.backend.mapper.UserMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * todo
 * 针对表【user(用户)】的数据库操作Service实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {


    /**
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        return 0;
    }

    /**
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        return null;
    }

    /**
     * @param userPassword
     * @return
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        return null;
    }

    /**
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        return null;
    }

    /**
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        return null;
    }

    /**
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        return null;
    }

    /**
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        return null;
    }

    /**
     * @param request
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        return false;
    }

    /**
     * @param userQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        return null;
    }

    /**
     * @param user
     * @return
     */
    @Override
    public boolean isAdmin(User user) {
        return false;
    }

    /**
     * @param user
     * @param vipCode
     * @return
     */
    @Override
    public boolean exchangeVip(User user, String vipCode) {
        return false;
    }
}