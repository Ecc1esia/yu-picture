package com.github.ecc1esia.picture.infrastructure.repository;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.ecc1esia.picture.domain.space.entity.Space;
import com.github.ecc1esia.picture.domain.space.repository.SpaceRepository;
import com.github.ecc1esia.picture.infrastructure.mapper.SpaceMapper;

@Service
public class SpaceRepositoryImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceRepository{

}
