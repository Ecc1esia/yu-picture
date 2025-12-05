package com.github.ecc1esia.picture.infrastructure.repository;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.ecc1esia.picture.domain.user.entity.User;
import com.github.ecc1esia.picture.domain.user.repository.UserRepository;
import com.github.ecc1esia.picture.infrastructure.mapper.UserMapper;

/**
 * 图片仓储实现
 */
@Service
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User> implements UserRepository {
}
