package com.github.ecc1esia.picture.infrastructure.api.imagesearch.tineye;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.ecc1esia.picture.infrastructure.api.imagesearch.model.ImageSearchResult;
import com.github.ecc1esia.picture.infrastructure.exception.BusinessException;
import com.github.ecc1esia.picture.infrastructure.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * TinEye 以图搜图 API
 */
@Slf4j
@Component
public class TinEyeApi {

    private static final String SEARCH_URL = "https://api.tineye.com/rest/search/";

    @Value("${tineye.api.username}")
    private String username;

    @Value("${tineye.api.password}")
    private String password;

    /**
     * 通过图片URL搜索相似图片
     */
    public List<ImageSearchResult> searchByImageUrl(String imageUrl) {
        String auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        try (HttpResponse response = HttpRequest.post(SEARCH_URL)
                .header("Authorization", "Basic " + auth)
                .form("image_url", imageUrl)
                .form("limit", 20)
                .timeout(10000)
                .execute()) {

            if (response.getStatus() != 200) {
                log.error("TinEye 搜索失败, status={}, body={}", response.getStatus(), response.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "TinEye 搜索失败");
            }

            JSONObject json = JSONUtil.parseObj(response.body());
            JSONArray results = json.getJSONArray("results");

            List<ImageSearchResult> list = new ArrayList<>();
            if (results != null) {
                for (int i = 0; i < results.size(); i++) {
                    JSONObject item = results.getJSONObject(i);
                    ImageSearchResult result = new ImageSearchResult();
                    result.setThumbUrl(item.getStr("image_url"));
                    result.setFromUrl(item.getStr("domain"));
                    list.add(result);
                }
            }
            log.info("TinEye 搜索完成, imageUrl={}, 结果数={}", imageUrl, list.size());
            return list;
        }
    }
}
