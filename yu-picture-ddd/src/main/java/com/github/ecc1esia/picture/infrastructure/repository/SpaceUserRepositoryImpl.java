package com.github.ecc1esia.picture.infrastructure.repository;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.ecc1esia.picture.domain.space.entity.SpaceUser;
import com.github.ecc1esia.picture.domain.space.repository.SpaceUserRepository;
import com.github.ecc1esia.picture.infrastructure.mapper.SpaceUserMapper;

@Service
public class SpaceUserRepositoryImpl extends ServiceImpl<SpaceUserMapper, SpaceUser> implements SpaceUserRepository {

}
