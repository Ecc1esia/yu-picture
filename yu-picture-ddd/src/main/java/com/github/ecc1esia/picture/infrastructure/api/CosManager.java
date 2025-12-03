package com.github.ecc1esia.picture.infrastructure.api;

import cn.hutool.core.io.FileUtil;
import com.github.ecc1esia.picture.infrastructure.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 对象存储管理
 */
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosclient;

    /**
     * 上传文件对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosclient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 唯一键
     * @return
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosclient.getObject(getObjectRequest);
    }

    /**
     * 上传图片对象，并进行图片处理
     *
     * @param key  唯一键
     * @param file 图片文件
     * @return
     */
    public PutObjectResult putPictureObject(String key, File file) {
        // 初始化图片操作对象
        PicOperations picOperations = new PicOperations();

        // 设置是否返回图片信息
        picOperations.setIsPicInfo(1);

        // 初始化图片处理规则列表
        List<PicOperations.Rule> rules = new ArrayList<>();

        // 创建上传请求对象
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);

        // 构造webp格式图片的键
        String webpKey = FileUtil.mainName(key) + ".webp";

        // 创建压缩规则对象
        PicOperations.Rule compressRule = new PicOperations.Rule();

        // 设置压缩图片的键和桶，并定义压缩规则
        compressRule.setFileId(webpKey);
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setRule("imageMogr2/format/webp");
        rules.add(compressRule);

        // 如果文件大小超过2KB，添加缩略图生成规则
        if (file.length() > 2 * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();

            // 构造缩略图的键
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);

            // 设置缩略图的键和桶，并定义缩略规则
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/!%sx%s", 256, 256));
            rules.add(thumbnailRule);
        }

        // 设置图片处理规则，并关联到上传请求
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);

        // 执行上传并返回结果
        return cosclient.putObject(putObjectRequest);
    }

    /**
     * 删除对象
     *
     * @param key 唯一键
     */
    public void deleteObject(String key) {
        cosclient.deleteObject(cosClientConfig.getBucket(), key);
    }
}
