package com.github.ecc1esia.picture.domain.picture.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.ecc1esia.picture.domain.picture.entity.Picture;
import com.github.ecc1esia.picture.domain.picture.service.PictureDomainService;
import com.github.ecc1esia.picture.domain.user.entity.User;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.github.ecc1esia.picture.infrastructure.exception.ErrorCode;
import com.github.ecc1esia.picture.infrastructure.exception.ThrowUtils;
import com.github.ecc1esia.picture.infrastructure.mapper.PictureMapper;
import com.github.ecc1esia.picture.interfaces.dto.picture.*;
import com.github.ecc1esia.picture.interfaces.vo.picture.PictureVO;

import groovy.util.logging.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

/**
 * todo
 */
@Slf4j
@Service
public class PictureDomainServiceImpl
        implements PictureDomainService {

    @Resource
    private PictureMapper pictureMapper;

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        return null;
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        return null;
    }

    @Override
    public void fillReviewParams(Picture picture, User loginUser) {

    }

    @Override
    public void clearPictureFile(Picture oldPicture) {

    }

    @Override
    public void deletePicture(long pictureId, User loginUser) {

    }

    /**
     * @param picture
     * @param loginUser
     */
    @Override
    public void editPicture(Picture picture, User loginUser) {
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        picture.validPicture();
        // 判断是否存在
        long id = picture.getId();
        Picture oldPicture = pictureMapper.selectById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 补充审核参数
        this.fillReviewParams(picture, loginUser);
        // 操作数据库
        int result = pictureMapper.updateById(picture);
        ThrowUtils.throwIf(result == 0, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {

    }

    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        return null;
    }

    @Override
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {

    }

    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(
            CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        return null;
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        return null;
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {

    }
}
