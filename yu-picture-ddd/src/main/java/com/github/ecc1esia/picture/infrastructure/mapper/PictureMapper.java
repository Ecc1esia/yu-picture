package com.github.ecc1esia.picture.infrastructure.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.ecc1esia.picture.domain.picture.entity.Picture;

/**
* @author ecc1esia
* @description 针对表【picture(图片)】的数据库操作Mapper
*/
@Mapper
public interface PictureMapper extends BaseMapper<Picture> {

}




