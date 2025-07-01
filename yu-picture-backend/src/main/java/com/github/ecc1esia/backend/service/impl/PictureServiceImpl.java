package com.github.ecc1esia.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.ecc1esia.backend.api.aliyunai.AliYunAiApi;
import com.github.ecc1esia.backend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.github.ecc1esia.backend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.github.ecc1esia.backend.exception.BusinessException;
import com.github.ecc1esia.backend.exception.ErrorCode;
import com.github.ecc1esia.backend.exception.ThrowUtils;
import com.github.ecc1esia.backend.manager.CosManager;
import com.github.ecc1esia.backend.manager.FileManager;
//import com.github.ecc1esia.backend.manager.upload.FilePictureUpload;
//import com.github.ecc1esia.backend.manager.upload.PictureUploadTemplate;
//import com.github.ecc1esia.backend.manager.upload.UrlPictureUpload;
//import com.github.ecc1esia.backend.mapper.PictureMapper;
import com.github.ecc1esia.backend.manager.upload.FilePictureUpload;
import com.github.ecc1esia.backend.manager.upload.UrlPictureUpload;
import com.github.ecc1esia.backend.mapper.PictureMapper;
import com.github.ecc1esia.backend.model.dto.file.UploadPictureResult;
import com.github.ecc1esia.backend.model.dto.picture.*;
import com.github.ecc1esia.backend.model.entity.Picture;
import com.github.ecc1esia.backend.model.entity.Space;
import com.github.ecc1esia.backend.model.entity.User;
import com.github.ecc1esia.backend.model.enums.PictureReviewStatusEnum;
import com.github.ecc1esia.backend.model.vo.PictureVO;
import com.github.ecc1esia.backend.model.vo.UserVO;
import com.github.ecc1esia.backend.service.PictureService;
import com.github.ecc1esia.backend.service.SpaceService;
import com.github.ecc1esia.backend.service.UserService;
import com.github.ecc1esia.backend.utils.ColorSimilarUtils;
import com.github.ecc1esia.backend.utils.ColorTransformUtils;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * todo
 *  针对表 picture 的数据库操作Service实现
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

//    @Resource
//    private FileManager fileManager;

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Autowired
    private CosManager cosManager;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private AliYunAiApi aliYunAiApi;


    /**
     * @param picture
     */
    @Override
    public void validPicture(Picture picture) {

    }

    /**
     * @param inputSource          文件输入源
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        return null;
    }

    /**
     * @param picture
     * @param request
     * @return
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        return null;
    }

    /**
     * @param picturePage
     * @param request
     * @return
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        return null;
    }

    /**
     * @param pictureQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        return null;
    }

    /**
     * @param pictureReviewRequest
     * @param loginUser
     */
    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {

    }

    /**
     * @param picture
     * @param loginUser
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {

    }

    /**
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return
     */
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        return null;
    }

    /**
     * @param oldPicture
     */
    @Override
    public void clearPictureFile(Picture oldPicture) {

    }

    /**
     * @param pictureId
     * @param loginUser
     */
    @Override
    public void deletePicture(long pictureId, User loginUser) {

    }

    /**
     * @param pictureEditRequest
     * @param loginUser
     */
    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {

    }

    /**
     * @param loginUser
     * @param picture
     */
    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {

    }

    /**
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        return null;
    }

    /**
     * @param pictureEditByBatchRequest
     * @param loginUser
     */
    @Override
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {

    }

    /**
     * @param createPictureOutPaintingTaskRequest
     * @param loginUser
     * @return
     */
    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        return null;
    }
}











