package com.github.ecc1esia.picture.domain.picture.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.ecc1esia.picture.domain.picture.entity.Picture;
import com.github.ecc1esia.picture.domain.picture.service.PictureDomainService;
import com.github.ecc1esia.picture.domain.user.entity.User;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.github.ecc1esia.picture.interfaces.dto.picture.*;
import com.github.ecc1esia.picture.interfaces.vo.picture.PictureVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PictureDomainServiceImpl implements PictureDomainService {


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

    @Override
    public void editPicture(Picture picture, User loginUser) {

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
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
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
