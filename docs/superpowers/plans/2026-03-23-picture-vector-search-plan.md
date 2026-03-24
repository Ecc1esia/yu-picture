# 图片向量检索功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现"同一空间内图片向量检索"功能，用户可以通过上传图片或选择已有图片，在同一空间内搜索相似图片。

**Architecture:** 使用阿里云百炼多模态 Embedding API 提取图片向量，存入 ElasticSearch，使用 KNN 检索相似图片。

**Tech Stack:** Spring Boot 3.2.5, ElasticSearch 8.11, MyBatis Plus, 阿里云百炼 DashScope API

---

## 前置问题修复

**现有 AliYunAiApi 代码Bug**（在开始实现前需修复）:

现有 `AliYunAiApi.java` 中有两处问题：

1. **@Value 注解语法错误**（第21行）:
```java
// 错误
@Value("{aliYunAi.apiKey}")

// 应修复为
@Value("${aliYunAi.apiKey}")
```

2. **Authorization header 缺少空格**（第44行）:
```java
// 错误
.header("Authorization", "Bearer" + apiKey)

// 应修复为
.header("Authorization", "Bearer " + apiKey)
```

这两个问题需要先修复，否则 API 调用会失败。

---

## 文件结构

```
yu-picture-ddd/src/main/java/com/github/ecc1esia/picture/
├── domain/picture/
│   ├── entity/
│   │   └── PictureVectorRecord.java          # ES 文档领域实体
│   └── service/
│       ├── PictureVectorService.java          # 领域服务接口
│       └── impl/
│           └── PictureVectorServiceImpl.java  # 领域服务实现
├── infrastructure/
│   ├── repository/
│   │   ├── PictureVectorRepository.java      # ES 仓储接口
│   │   └── impl/
│   │       └── PictureVectorRepositoryImpl.java  # ES 仓储实现
│   └── api/aliyunai/
│       └── AliYunAiApi.java                   # 扩展多模态embedding方法
├── interfaces/
│   ├── controller/
│   │   └── PictureVectorController.java       # 向量搜索控制器
│   ├── dto/picture/
│   │   └── SearchVectorByPictureRequest.java  # by-picture请求DTO
│   └── vo/picture/
│       └── VectorSearchResultVO.java          # 搜索结果VO
```

---

## Task 1: 添加 ElasticSearch 依赖

**Files:**
- Modify: `yu-picture-ddd/pom.xml`
- Test: 运行 `mvn dependency:tree | grep elastic` 验证

- [ ] **Step 1: 添加 ES 依赖**

在 `pom.xml` 的 `<dependencies>` 中添加：

```xml
<!-- ElasticSearch -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

- [ ] **Step 2: 验证依赖**

Run: `cd yu-picture-ddd && mvn dependency:tree -Dincludes=*:elasticsearch* | head -20`
Expected: 显示 elasticsearch 相关依赖

---

## Task 2: 配置 application.yaml

**Files:**
- Modify: `yu-picture-ddd/src/main/resources/application.yaml`

- [ ] **Step 1: 添加 ES 和百炼配置**

在 `application.yaml` 末尾添加：

```yaml
# ElasticSearch 配置
spring:
  elasticsearch:
    uris: http://localhost:9200

# 阿里云百炼 API 配置
aliyun:
  dashscope:
    api-key: your-api-key-here
```

---

## Task 3: 创建 PictureVectorRecord 领域实体

**Files:**
- Create: `yu-picture-ddd/src/main/java/com/github/ecc1esia/picture/domain/picture/entity/PictureVectorRecord.java`

- [ ] **Step 1: 创建领域实体（包含score字段）**

```java
package com.github.ecc1esia.picture.domain.picture.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 图片向量记录（ES 文档实体）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PictureVectorRecord {

    /**
     * 图片 ID
     */
    private Long pictureId;

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 原图 URL
     */
    private String url;

    /**
     * 缩略图 URL
     */
    private String thumbnailUrl;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 图片向量（512维，注意：维度需与阿里云百炼API返回一致）
     */
    private float[] vector;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 相似度分数（搜索时返回，非持久化字段）
     */
    private Double score;
}
```

---

## Task 4: 创建 PictureVectorRepository 接口

**Files:**
- Create: `yu-picture-ddd/src/main/java/com/github/ecc1esia/picture/infrastructure/repository/PictureVectorRepository.java`

- [ ] **Step 1: 创建仓储接口**

```java
package com.github.ecc1esia.picture.infrastructure.repository;

import com.github.ecc1esia.picture.domain.picture.entity.PictureVectorRecord;

import java.util.List;

/**
 * 图片向量仓储接口
 */
public interface PictureVectorRepository {

    /**
     * 创建索引（启动时调用）
     */
    void createIndexIfNotExists();

    /**
     * 保存图片向量
     * @param record 向量记录
     */
    void save(PictureVectorRecord record);

    /**
     * KNN 搜索
     * @param spaceId 空间 ID
     * @param queryVector 查询向量
     * @param topK 返回数量
     * @return 匹配的记录列表
     */
    List<PictureVectorRecord> search(Long spaceId, float[] queryVector, int topK);

    /**
     * 根据图片ID删除向量
     * @param pictureId 图片 ID
     */
    void deleteByPictureId(Long pictureId);
}
```

---

## Task 5: 实现 PictureVectorRepositoryImpl

**Files:**
- Create: `yu-picture-ddd/src/main/java/com/github/ecc1esia/picture/infrastructure/repository/impl/PictureVectorRepositoryImpl.java`

- [ ] **Step 1: 实现 ES 仓储**

```java
package com.github.ecc1esia.picture.infrastructure.repository.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.KnnQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.github.ecc1esia.picture.domain.picture.entity.PictureVectorRecord;
import com.github.ecc1esia.picture.domain.picture.repository.PictureVectorRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片向量仓储实现（ES）
 */
@Slf4j
@Repository
public class PictureVectorRepositoryImpl implements PictureVectorRepository {

    private static final String INDEX_NAME = "picture_vector";
    private static final int VECTOR_DIMENSION = 512; // 注意：需与阿里云百炼API返回维度一致

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @PostConstruct
    @Override
    public void createIndexIfNotExists() {
        try {
            boolean exists = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(INDEX_NAME)))
                    .value();
            if (!exists) {
                elasticsearchClient.indices().create(CreateIndexRequest.of(c -> c
                        .index(INDEX_NAME)
                        .settings(s -> s
                                .numberOfShards("1")
                                .numberOfReplicas("0")
                        )
                        .mappings(m -> m
                                .properties("pictureId", p -> p.long_(l -> l))
                                .properties("spaceId", p -> p.long_(l -> l))
                                .properties("url", p -> p.keyword_(k -> k))
                                .properties("thumbnailUrl", p -> p.keyword_(k -> k))
                                .properties("name", p -> p.text(t -> t))
                                .properties("vector", p -> p.denseVector(d -> d
                                        .dims(VECTOR_DIMENSION)
                                        .index(true)
                                        .similarity("cosine")
                                ))
                                .properties("createTime", p -> p.date(d -> d))
                        )
                ));
                log.info("ES索引 {} 创建成功", INDEX_NAME);
            }
        } catch (IOException e) {
            log.error("创建ES索引失败", e);
        }
    }

    @Override
    public void save(PictureVectorRecord record) {
        try {
            IndexResponse response = elasticsearchClient.index(IndexRequest.of(i -> i
                    .index(INDEX_NAME)
                    .id(String.valueOf(record.getPictureId()))
                    .document(record)
            ));
            log.debug("向量保存成功, pictureId={}, result={}", record.getPictureId(), response.result());
        } catch (IOException e) {
            log.error("保存向量失败, pictureId={}", record.getPictureId(), e);
            throw new RuntimeException("保存向量失败", e);
        }
    }

    @Override
    public List<PictureVectorRecord> search(Long spaceId, float[] queryVector, int topK) {
        try {
            // ES 8.x KNN搜索：使用post_filter在KNN搜索后过滤spaceId
            SearchResponse<PictureVectorRecord> response = elasticsearchClient.search(SearchRequest.of(s -> s
                            .index(INDEX_NAME)
                            .knn(KnnQuery.of(k -> k
                                    .field("vector")
                                    .vector(queryVector)
                                    .k(topK)
                                    .numCandidates(topK * 2)
                            ))
                            .postFilter(Query.of(q -> q
                                    .term(T -> T.field("spaceId").value(spaceId))
                            ))
                            .size(topK)
                    ),
                    PictureVectorRecord.class
            );

            List<PictureVectorRecord> results = new ArrayList<>();
            for (Hit<PictureVectorRecord> hit : response.hits().hits()) {
                PictureVectorRecord record = hit.source();
                if (record != null) {
                    // 设置相似度分数
                    record.setScore(hit.score() != null ? hit.score().doubleValue() : 0.0);
                    results.add(record);
                }
            }
            return results;
        } catch (IOException e) {
            log.error("向量搜索失败, spaceId={}", spaceId, e);
            throw new RuntimeException("向量搜索失败", e);
        }
    }

    @Override
    public void deleteByPictureId(Long pictureId) {
        try {
            elasticsearchClient.delete(DeleteRequest.of(d -> d
                    .index(INDEX_NAME)
                    .id(String.valueOf(pictureId))
            ));
            log.debug("向量删除成功, pictureId={}", pictureId);
        } catch (IOException e) {
            log.error("删除向量失败, pictureId={}", pictureId, e);
        }
    }
}
```

**注意:** `PictureVectorRecord` 需要添加 `score` 字段用于存储相似度分数。

---

## Task 7: 扩展 AliYunAiApi 添加 extractImageVector 方法

**Files:**
- Modify: `yu-picture-ddd/src/main/java/com/github/ecc1esia/picture/infrastructure/api/aliyunai/AliYunAiApi.java`

- [ ] **Step 1: 添加配置和抽取向量方法**

在 `AliYunAiApi` 类中添加：

```java
// 百炼多模态Embedding API地址
public static final String MULTIMODAL_EMBEDDING_URL = "https://dashscope.aliyuncs.com/api/v1/services/embeddings/multimodal/embedding";

// 百炼API Key配置（复用现有的aliYunAi.apiKey配置）
// 注意：现有AliYunAiApi已有@Value("${aliYunAi.apiKey}")，可直接复用
// 如果使用独立的百炼Key，添加配置: aliyun.dashscope.api-key

/**
 * 提取图片向量（使用百炼多模态Embedding API）
 * @param imageUrl 图片URL（公网可访问）
 * @return float[] 向量数组
 */
public float[] extractImageVector(String imageUrl) {
    if (StrUtil.isBlank(imageUrl)) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片URL不能为空");
    }

    // 构建请求体
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", "qwen-vl-embedding"); // 多模态embedding模型

    List<Map<String, String>> input = new ArrayList<>();
    Map<String, String> imageItem = new HashMap<>();
    imageItem.put("image", imageUrl);
    input.add(imageItem);

    Map<String, Object> inputWrapper = new HashMap<>();
    inputWrapper.put("input", input);
    requestBody.put("input", inputWrapper);

    try {
        HttpResponse httpResponse = HttpRequest.post(MULTIMODAL_EMBEDDING_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(requestBody))
                .execute();

        if (!httpResponse.isOk()) {
            log.error("提取图片向量失败: {}", httpResponse.body());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "提取图片向量失败");
        }

        JSONObject jsonObject = JSONUtil.parseObj(httpResponse.body());
        // 解析返回的向量数据（具体格式需参考阿里云百炼API文档调整）
        JSONObject data = jsonObject.getJSONObject("data");
        if (data == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "提取图片向量返回数据格式错误");
        }

        JSONArray embeddings = data.getJSONArray("embeddings");
        if (embeddings == null || embeddings.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "提取图片向量返回为空");
        }

        JSONObject firstEmbedding = embeddings.getJSONObject(0);
        JSONArray embeddingVector = firstEmbedding.getJSONArray("embedding");

        float[] vector = new float[embeddingVector.size()];
        for (int i = 0; i < embeddingVector.size(); i++) {
            vector[i] = embeddingVector.getFloat(i);
        }

        log.info("成功提取图片向量, imageUrl={}, dimension={}", imageUrl, vector.length);
        return vector;

    } catch (BusinessException e) {
        throw e;
    } catch (Exception e) {
        log.error("提取图片向量异常, imageUrl={}", imageUrl, e);
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "提取图片向量异常: " + e.getMessage());
    }
}
```

**注意:** 阿里云百炼多模态Embedding API的具体返回格式需要参考官方文档，以上代码为示例结构，需根据实际API返回格式调整JSON解析逻辑。

---

## Task 8: 创建 PictureVectorService 领域服务接口

**Files:**
- Create: `yu-picture-ddd/src/main/java/com/github/ecc1esia/picture/domain/picture/service/PictureVectorService.java`

- [ ] **Step 1: 创建领域服务接口**

```java
package com.github.ecc1esia.picture.domain.picture.service;

import com.github.ecc1esia.picture.domain.picture.entity.Picture;
import com.github.ecc1esia.picture.domain.picture.entity.PictureVectorRecord;

import java.util.List;

/**
 * 图片向量服务接口
 */
public interface PictureVectorService {

    /**
     * 保存图片向量到ES
     * @param picture 图片实体
     */
    void savePictureVector(Picture picture);

    /**
     * 通过图片URL提取向量并搜索相似图片
     * @param spaceId 空间ID
     * @param imageUrl 图片URL
     * @param topK 返回数量
     * @return 相似图片列表
     */
    List<PictureVectorRecord> searchSimilarPictures(Long spaceId, String imageUrl, int topK);

    /**
     * 通过已有图片ID搜索相似图片
     * @param spaceId 空间ID
     * @param pictureId 已有图片ID
     * @param topK 返回数量
     * @return 相似图片列表
     */
    List<PictureVectorRecord> searchSimilarPicturesById(Long spaceId, Long pictureId, int topK);

    /**
     * 删除图片向量
     * @param pictureId 图片ID
     */
    void deletePictureVector(Long pictureId);
}
```

---

## Task 9: 实现 PictureVectorServiceImpl

**Files:**
- Create: `yu-picture-ddd/src/main/java/com/github/ecc1esia/picture/domain/picture/service/impl/PictureVectorServiceImpl.java`

- [ ] **Step 1: 实现领域服务**

```java
package com.github.ecc1esia.picture.domain.picture.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.ecc1esia.picture.domain.picture.entity.Picture;
import com.github.ecc1esia.picture.domain.picture.entity.PictureVectorRecord;
import com.github.ecc1esia.picture.domain.picture.service.PictureVectorService;
import com.github.ecc1esia.picture.domain.picture.repository.PictureRepository;
import com.github.ecc1esia.picture.infrastructure.api.aliyunai.AliYunAiApi;
import com.github.ecc1esia.picture.infrastructure.exception.BusinessException;
import com.github.ecc1esia.picture.infrastructure.exception.ErrorCode;
import com.github.ecc1esia.picture.infrastructure.exception.ThrowUtils;
import com.github.ecc1esia.picture.domain.picture.repository.PictureVectorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图片向量服务实现
 */
@Slf4j
@Service
public class PictureVectorServiceImpl implements PictureVectorService {

    @Resource
    private AliYunAiApi aliYunAiApi;

    @Resource
    private PictureVectorRepository pictureVectorRepository;

    @Resource
    private PictureRepository pictureRepository;

    @Override
    public void savePictureVector(Picture picture) {
        if (picture == null || picture.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片信息不完整");
        }

        try {
            // 提取图片向量
            float[] vector = aliYunAiApi.extractImageVector(picture.getUrl());

            // 构建向量记录
            PictureVectorRecord record = PictureVectorRecord.builder()
                    .pictureId(picture.getId())
                    .spaceId(picture.getSpaceId())
                    .url(picture.getUrl())
                    .thumbnailUrl(picture.getThumbnailUrl())
                    .name(picture.getName())
                    .vector(vector)
                    .createTime(LocalDateTime.now())
                    .build();

            // 保存到ES
            pictureVectorRepository.save(record);
            log.info("图片向量保存成功, pictureId={}", picture.getId());

        } catch (Exception e) {
            log.error("保存图片向量失败, pictureId={}", picture.getId(), e);
            // 向量保存失败不影响主流程，仅记录日志
        }
    }

    @Override
    public List<PictureVectorRecord> searchSimilarPictures(Long spaceId, String imageUrl, int topK) {
        if (spaceId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间ID不能为空");
        }
        if (StrUtil.isBlank(imageUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片URL不能为空");
        }

        try {
            // 提取查询图片的向量
            float[] queryVector = aliYunAiApi.extractImageVector(imageUrl);

            // ES KNN搜索
            List<PictureVectorRecord> results = pictureVectorRepository.search(spaceId, queryVector, topK);

            // 过滤已删除的图片
            return filterDeletedPictures(results);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("向量搜索失败, spaceId={}", spaceId, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片搜索失败");
        }
    }

    @Override
    public List<PictureVectorRecord> searchSimilarPicturesById(Long spaceId, Long pictureId, int topK) {
        if (spaceId == null || pictureId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不完整");
        }

        // 查询源图片
        Picture sourcePicture = pictureRepository.getById(pictureId);
        ThrowUtils.throwIf(sourcePicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        // 校验图片是否属于指定空间
        if (!spaceId.equals(sourcePicture.getSpaceId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片不在指定空间中");
        }

        return searchSimilarPictures(spaceId, sourcePicture.getUrl(), topK);
    }

    @Override
    public void deletePictureVector(Long pictureId) {
        if (pictureId == null) {
            return;
        }
        pictureVectorRepository.deleteByPictureId(pictureId);
        log.info("图片向量删除成功, pictureId={}", pictureId);
    }

    /**
     * 过滤已删除的图片
     */
    private List<PictureVectorRecord> filterDeletedPictures(List<PictureVectorRecord> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        List<Long> pictureIds = records.stream()
                .map(PictureVectorRecord::getPictureId)
                .collect(Collectors.toList());

        // 批量查询图片是否存在
        List<Picture> pictures = pictureRepository.listByIds(pictureIds);

        // 转换为Set用于快速查找
        List<Long> existingIds = pictures.stream()
                .filter(p -> p.getIsDelete() == 0) // 未删除
                .map(Picture::getId)
                .collect(Collectors.toList());

        // 过滤
        return records.stream()
                .filter(r -> existingIds.contains(r.getPictureId()))
                .collect(Collectors.toList());
    }
}
```

**注意:** 需要注入 `cn.hutool.core.util.StrUtil` 用于字符串判空检查。

---

## Task 10: 创建 VectorSearchResultVO

**Files:**
- Create: `yu-picture-ddd/src/main/java/com/github/ecc1esia/picture/interfaces/vo/picture/VectorSearchResultVO.java`

- [ ] **Step 1: 创建响应VO**

```java
package com.github.ecc1esia.picture.interfaces.vo.picture;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 向量搜索结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorSearchResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图片ID
     */
    private Long pictureId;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 原图URL
     */
    private String url;

    /**
     * 相似度分值 (0-1)
     */
    private Double score;
}
```

---

## Task 11: 创建 SearchVectorByPictureRequest DTO

**Files:**
- Create: `yu-picture-ddd/src/main/java/com/github/ecc1esia/picture/interfaces/dto/picture/SearchVectorByPictureRequest.java`

- [ ] **Step 1: 创建请求DTO**

```java
package com.github.ecc1esia.picture.interfaces.dto.picture;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 按已有图片搜索向量请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchVectorByPictureRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 已有的图片ID
     */
    private Long pictureId;

    /**
     * 目标空间ID
     */
    private Long spaceId;
}
```

---

## Task 12: 创建 PictureVectorController

**Files:**
- Create: `yu-picture-ddd/src/main/java/com/github/ecc1esia/picture/interfaces/controller/PictureVectorController.java`

- [ ] **Step 1: 创建控制器**

```java
package com.github.ecc1esia.picture.interfaces.controller;

import com.github.ecc1esia.picture.domain.picture.entity.PictureVectorRecord;
import com.github.ecc1esia.picture.domain.picture.service.PictureVectorService;
import com.github.ecc1esia.picture.domain.user.entity.User;
import com.github.ecc1esia.picture.application.service.UserApplicationService;
import com.github.ecc1esia.picture.infrastructure.common.BaseResponse;
import com.github.ecc1esia.picture.infrastructure.common.ResultUtils;
import com.github.ecc1esia.picture.infrastructure.exception.BusinessException;
import com.github.ecc1esia.picture.infrastructure.exception.ErrorCode;
import com.github.ecc1esia.picture.infrastructure.exception.ThrowUtils;
import com.github.ecc1esia.picture.interfaces.dto.picture.SearchVectorByPictureRequest;
import com.github.ecc1esia.picture.interfaces.vo.picture.VectorSearchResultVO;
import com.github.ecc1esia.picture.shared.auth.annotation.SaSpaceCheckPermission;
import com.github.ecc1esia.picture.shared.auth.model.SpaceUserPermissionConstant;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.request.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 图片向量搜索控制器
 */
@Slf4j
@RestController
@RequestMapping("/picture/search/vector")
public class PictureVectorController {

    private static final int DEFAULT_TOP_K = 10;
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/jpg", "image/bmp", "image/webp"
    );

    @Resource
    private PictureVectorService pictureVectorService;

    @Resource
    private UserApplicationService userApplicationService;

    /**
     * 上传图片搜图
     */
    @PostMapping("/by-upload")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<VectorSearchResultVO>> searchByUpload(
            @RequestPart("file") MultipartFile file,
            @RequestParam("spaceId") Long spaceId,
            HttpServletRequest request) {

        // 参数校验
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "图片文件不能为空");
        ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR, "空间ID不能为空");

        // 校验文件类型
        String contentType = file.getContentType();
        ThrowUtils.throwIf(!ALLOWED_IMAGE_TYPES.contains(contentType),
                ErrorCode.PARAMS_ERROR, "只能上传图片文件");

        // 获取登录用户
        User loginUser = userApplicationService.getLoginUser(request);

        // 临时保存上传的图片获取URL（实际项目中可能需要先上传到COS）
        // 这里简化处理，实际应该先上传到COS获取URL
        String tempImageUrl = uploadToCosAndGetUrl(file);

        // 搜索相似图片
        List<PictureVectorRecord> results = pictureVectorService.searchSimilarPictures(
                spaceId, tempImageUrl, DEFAULT_TOP_K);

        // 转换结果
        List<VectorSearchResultVO> voList = results.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return ResultUtils.success(voList);
    }

    /**
     * 选择已有图片搜图
     */
    @PostMapping("/by-picture")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<VectorSearchResultVO>> searchByPicture(
            @RequestBody SearchVectorByPictureRequest request,
            HttpServletRequest httpRequest) {

        // 参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(request.getPictureId() == null, ErrorCode.PARAMS_ERROR, "图片ID不能为空");
        ThrowUtils.throwIf(request.getSpaceId() == null, ErrorCode.PARAMS_ERROR, "空间ID不能为空");

        // 获取登录用户
        User loginUser = userApplicationService.getLoginUser(httpRequest);

        // 搜索相似图片
        List<PictureVectorRecord> results = pictureVectorService.searchSimilarPicturesById(
                request.getSpaceId(), request.getPictureId(), DEFAULT_TOP_K);

        // 转换结果
        List<VectorSearchResultVO> voList = results.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return ResultUtils.success(voList);
    }

    /**
     * 将上传文件临时保存到COS并返回URL
     * TODO: 实现COS上传逻辑
     */
    private String uploadToCosAndGetUrl(MultipartFile file) {
        // TODO: 实现实际的上传逻辑
        // 这里抛出异常，提示需要实现
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传图片搜图功能待实现，需先完成COS上传逻辑");
    }

    /**
     * 转换为VO
     */
    private VectorSearchResultVO convertToVO(PictureVectorRecord record) {
        return VectorSearchResultVO.builder()
                .pictureId(record.getPictureId())
                .name(record.getName())
                .thumbnailUrl(record.getThumbnailUrl())
                .url(record.getUrl())
                .score(record.getScore())
                .build();
    }
}
```

---

## Task 13: 添加缺失的 import

**Files:**
- Modify: `yu-picture-ddd/src/main/java/com/github/ecc1esia/picture/domain/picture/service/impl/PictureVectorServiceImpl.java`

- [ ] **Step 1: 添加 import**

```java
import cn.hutool.core.util.StrUtil;
import com.github.ecc1esia.picture.domain.picture.entity.Picture;
import com.github.ecc1esia.picture.domain.picture.entity.PictureVectorRecord;
import com.github.ecc1esia.picture.domain.picture.service.PictureVectorService;
import com.github.ecc1esia.picture.domain.picture.repository.PictureRepository;
```

---

## Task 14: 创建 PictureRepository 接口

**Files:**
- Create: `yu-picture-ddd/src/main/java/com/github/ecc1esia/picture/domain/picture/repository/PictureRepository.java`

- [ ] **Step 1: 创建仓储接口**

```java
package com.github.ecc1esia.picture.domain.picture.repository;

import com.github.ecc1esia.picture.domain.picture.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 图片仓储接口
 */
public interface PictureRepository extends IService<Picture> {

    /**
     * 根据ID列表批量查询
     * @param ids ID列表
     * @return 图片列表
     */
    List<Picture> listByIds(List<Long> ids);
}
```

---

## Task 15: 添加 COS 上传支持（by-upload 接口）

**Files:**
- Modify: `yu-picture-ddd/src/main/java/com/github/ecc1esia/picture/interfaces/controller/PictureVectorController.java`

- [ ] **Step 1: 实现COS上传方法**

需要注入 COS 客户端并实现 `uploadToCosAndGetUrl` 方法：

```java
@Resource
private CosClient cosClient; // 注入腾讯云COS客户端

private static final String COS_BUCKET = "your-bucket";
private static final String COS_REGION = "your-region";

/**
 * 将上传文件保存到COS并返回URL
 */
private String uploadToCosAndGetUrl(MultipartFile file) {
    try {
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String key = "temp/vector-search/" + UUID.randomUUID().toString() + extension;

        // 上传到COS
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(COS_BUCKET)
                .key(key)
                .inputStream(file.getInputStream())
                .build();
        cosClient.putObject(putObjectRequest);

        // 返回公网访问URL
        return String.format("https://%s.cos.%s.myqcloud.com/%s", COS_BUCKET, COS_REGION, key);

    } catch (Exception e) {
        log.error("上传文件到COS失败", e);
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片上传失败");
    }
}
```

---

## Task 16: 集成向量提取到图片上传流程

**Files:**
- Modify: `yu-picture-ddd/src/main/java/com/github/ecc1esia/picture/domain/picture/service/impl/PictureDomainServiceImpl.java`

- [ ] **Step 1: 在图片上传后提取向量**

找到 `uploadPicture` 方法，在图片上传成功后调用 `pictureVectorService.savePictureVector(picture)`：

```java
@Resource
private PictureVectorService pictureVectorService;

// 在uploadPicture成功保存后添加:
pictureVectorService.savePictureVector(picture);
```

---

## Task 17: 前端 - 添加 API 调用函数

**Files:**
- Modify: `yu-picture-web/src/api/pictureController.ts`

- [ ] **Step 1: 添加向量搜索API函数**

```typescript
/**
 * 按上传图片搜索相似图片
 */
export const searchVectorByUpload = (file: File, spaceId: number) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('spaceId', spaceId.toString());
  return request.post('/api/picture/search/vector/by-upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

/**
 * 按已有图片搜索相似图片
 */
export const searchVectorByPicture = (pictureId: number, spaceId: number) => {
  return request.post('/api/picture/search/vector/by-picture', {
    pictureId,
    spaceId,
  });
};
```

---

## Task 18: 前端 - 创建向量搜索结果展示组件

**Files:**
- Create: `yu-picture-web/src/components/VectorSearchResult.vue`

- [ ] **Step 1: 创建结果展示组件**

```vue
<template>
  <div class="vector-search-result">
    <a-row :gutter="[16, 16]">
      <a-col :span="6" v-for="item in results" :key="item.pictureId">
        <a-card hoverable @click="onPictureClick(item)">
          <template #cover>
            <img :src="item.thumbnailUrl" :alt="item.name" />
          </template>
          <a-card-meta :title="item.name">
            <template #description>
              <div>相似度: {{ (item.score * 100).toFixed(1) }}%</div>
            </template>
          </a-card-meta>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
interface VectorSearchResultVO {
  pictureId: number;
  name: string;
  thumbnailUrl: string;
  url: string;
  score: number;
}

defineProps<{
  results: VectorSearchResultVO[];
}>();

const emit = defineEmits<{
  (e: 'pictureClick', item: VectorSearchResultVO): void;
}>();

const onPictureClick = (item: VectorSearchResultVO) => {
  emit('pictureClick', item);
};
</script>
```

---

## Task 19: 验证向量维度

**Files:**
- Test: 编写测试用例验证

- [ ] **Step 1: 启动ES后验证向量维度**

首次部署后，需要验证阿里云百炼API返回的向量维度是否与代码中预设的 `VECTOR_DIMENSION = 512` 一致。如果不一致，需要修改 `PictureVectorRepositoryImpl` 中的维度设置。

---

## 部署检查清单

1. [ ] Docker 部署 ElasticSearch 8.11
2. [ ] 申请阿里云百炼 API Key
3. [ ] 配置 `application.yaml` 中的 ES 地址和 API Key
4. [ ] 验证向量维度与ES Mapping一致
5. [ ] 测试向量提取和搜索功能

---

## 注意事项

1. **向量维度**: 需在首次调用后确认阿里云百炼返回的实际维度，目前代码中假设为512维
2. **COS URL**: by-upload接口需要公网可访问的图片URL，腾讯云COS默认支持
3. **错误处理**: 向量提取失败不影响主流程（图片仍可上传），仅记录日志
4. **性能**: 向量提取依赖外部API，搜索可能有延迟
