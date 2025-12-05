package com.github.ecc1esia.picture.infrastructure.mapper;

import com.github.ecc1esia.picture.domain.space.entity.Space;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author ecc1esia
* @description 针对表【space(空间)】的数据库操作Mapper
* @createDate 2024-12-18 19:53:34
* @Entity com.github.ecc1esia.picture.domain.space.entity.Space
*/
@Mapper
public interface SpaceMapper extends BaseMapper<Space> {

}




