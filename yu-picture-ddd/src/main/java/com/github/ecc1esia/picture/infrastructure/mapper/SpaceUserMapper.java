package com.github.ecc1esia.picture.infrastructure.mapper;

import com.github.ecc1esia.picture.domain.space.entity.SpaceUser;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author ecc1esia
* @description 针对表【space_user(空间用户关联)】的数据库操作Mapper
* @createDate 2025-01-02 20:07:15
* @Entity com.github.ecc1esia.picture.domain.space.entity.SpaceUser
*/
@Mapper
public interface SpaceUserMapper extends BaseMapper<SpaceUser> {

}




