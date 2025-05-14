package com.github.ecc1esia.picture.interfaces.assembler;

import cn.hutool.json.JSONUtil;
import com.github.ecc1esia.picture.domain.picture.entity.Picture;
import com.github.ecc1esia.picture.interfaces.dto.picture.PictureEditRequest;
import com.github.ecc1esia.picture.interfaces.dto.picture.PictureUpdateRequest;
import org.springframework.beans.BeanUtils;

/**
 * 图片对象转换
 */
public class PictureAssembler {

    public static Picture toPictureEntity(PictureEditRequest request) {
        Picture picture = new Picture();
        BeanUtils.copyProperties(request, picture);
        // list转化为string
        picture.setTags(JSONUtil.toJsonStr(request.getTags()));
        return picture;
    }

    public static Picture toPictureEntity(PictureUpdateRequest request) {
        Picture picture = new Picture();
        BeanUtils.copyProperties(request, picture);
        // list转化为string
        picture.setTags(JSONUtil.toJsonStr(request.getTags()));
        return picture;
    }
}
