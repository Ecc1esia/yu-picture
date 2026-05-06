package com.github.ecc1esia.picture.infrastructure.api.imagesearch;


import com.github.ecc1esia.picture.infrastructure.api.imagesearch.model.ImageSearchResult;
import com.github.ecc1esia.picture.infrastructure.api.imagesearch.sub.GetImageFirstUrlApi;
import com.github.ecc1esia.picture.infrastructure.api.imagesearch.sub.GetImageListApi;
import com.github.ecc1esia.picture.infrastructure.api.imagesearch.sub.GetImagePageUrlApi;
import com.github.ecc1esia.picture.infrastructure.api.imagesearch.tineye.TinEyeApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ImageSearchApiFacade {

    @Value("${image.search.provider:baidu}")
    private String provider;

    @Resource
    private TinEyeApi tinEyeApi;

    /**
     * 搜索图片
     */
    public List<ImageSearchResult> searchImage(String imageUrl) {
        if ("tineye".equalsIgnoreCase(provider)) {
            return tinEyeApi.searchByImageUrl(imageUrl);
        }
        return baiduSearch(imageUrl);
    }

    private List<ImageSearchResult> baiduSearch(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        return GetImageListApi.getImageList(imageFirstUrl);
    }
}
