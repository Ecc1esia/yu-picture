package com.github.ecc1esia.picture.infrastructure.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.github.ecc1esia.picture.infrastructure.exception.BusinessException;
import com.github.ecc1esia.picture.infrastructure.exception.ErrorCode;
import com.github.ecc1esia.picture.infrastructure.exception.ThrowUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * URL图片上传类，继承自PictureUploadTemplate模板类
 * 该类用于处理通过URL地址上传的图片，确保图片的有效性和合规性
 */
public class UrlPictureUpload extends PictureUploadTemplate {

    /**
     * 验证图片的有效性和合规性
     * @param inputSource 图片的URL地址
     * 该方法首先检查文件地址是否为空，然后验证地址格式是否正确，最后检查文件类型和大小是否符合要求
     */
    @Override
    protected void validPicture(Object inputSource) {
        // 获取文件URL
        String fileUrl = (String) inputSource;
        // 检查文件地址是否为空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址为空");

        try {
            // 验证URL格式
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }
        // 检查URL协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址"
        );

        HttpResponse httpResponse = null;

        try {
            // 发起HEAD请求，获取文件信息
            httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();

            // 检查HTTP状态码
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }

            // 获取并检查文件类型
            String contentType = httpResponse.header("Content_type");

            if (StrUtil.isNotBlank(contentType)) {
                final List<String> ALLOW_CONTENT_TYPE = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");

                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPE.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误"
                );
            }

            // 获取并检查文件大小
            String contentLengthStr = httpResponse.header("Content_Length");

            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long ONE_M = 1024 * 1024;
                    ThrowUtils.throwIf(contentLength > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过2MB");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式异常");
                }
            }
        } finally {
            // 关闭HTTP响应
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
    }

    /**
     * 获取文件的原始名称
     * @param inputSource 图片的URL地址
     * @return 文件的原始名称，不包含扩展名
     */
    @Override
    protected String getOriginFilename(Object inputSource) {
        // 获取文件URL
        String fileUrl = (String) inputSource;
        // 使用FileUtil.mainName方法提取文件名
        return FileUtil.mainName(fileUrl);
    }

    /**
     * 处理文件下载
     * @param inputSource 图片的URL地址
     * @param file 目标文件对象，用于保存下载的文件
     * @throws Exception 文件下载过程中可能抛出的异常
     */
    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        // 获取文件URL
        String fileUrl = (String) inputSource;
        // 使用HttpUtil.downloadFile方法下载文件
        HttpUtil.downloadFile(fileUrl, file);
    }
}
