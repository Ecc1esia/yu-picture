package com.github.ecc1esia.picture.infrastructure.mapper;

import com.github.ecc1esia.picture.domain.user.entity.User;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author 李鱼皮
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2024-12-09 20:03:03
* @Entity com.github.ecc1esia.picture.domain.user.entity.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




