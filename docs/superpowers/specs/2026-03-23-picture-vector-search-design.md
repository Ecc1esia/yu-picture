# 图片向量检索功能设计

## 1. 需求概述

在毕设项目中实现"同一空间内图片向量检索"功能，用户可以通过上传图片或选择已有图片，在同一空间内搜索相似图片。

**技术选型：**
- 向量提取：**阿里云百炼（DashScope）多模态 Embedding API**（基于 Qwen2-VL）
- 向量存储与检索：ElasticSearch（单机 Docker 部署）
- 不使用 EasyES，使用原生 ES Java Client

---

## 0. 需要采购/开通的云服务

### 阿里云百炼（DashScope）

**产品页面：** https://www.aliyun.com/product/dashscope

**需要开通的服务：**
1. **百炼大模型服务** - 提供多模态 Embedding API
2. **API Key** - 在阿里云控制台申请，用于调用 API

**计费：** 按 token 计费，图片按张数折算 token。毕设场景用量有限，费用很低。

**API 文档：** https://help.aliyun.com/document_detail/272836.html

**多模态 Embedding API 特点：**
- 输入：图片 URL（公网可访问）或 base64 编码图片
- 输出：float 向量数组
- 向量维度：需实测确认（通常 512 或 1024）
- 支持 cosine 相似度计算

---

## 2. 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         用户请求                                  │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    PictureController                             │
│  POST /api/picture/search/vector/by-upload                      │
│  POST /api/picture/search/vector/by-picture                      │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                PictureApplicationService                         │
│  - searchPictureByVectorByUpload()                               │
│  - searchPictureByVectorByPictureId()                            │
└─────────────────────────────────────────────────────────────────┘
                                │
                ┌───────────────┴───────────────┐
                ▼                               ▼
┌───────────────────────────┐   ┌───────────────────────────────┐
│   AliYunAiApi             │   │   PictureDomainService        │
│   (百炼多模态embedding)     │   │   (向量搜索业务逻辑)           │
└───────────────────────────┘   └───────────────────────────────┘
                │                               │
                ▼                               ▼
┌─────────────────────────────────────────────────────────────────┐
│              PictureVectorRepository                            │
│              (ES 存储与检索)                                      │
└─────────────────────────────────────────────────────────────────┘
```

**核心流程：**

1. **图片上传时**（已有流程）：图片存储到腾讯云 COS
2. **向量入库**（新增）：调用阿里云百炼多模态 Embedding API 提取图片向量，存入 ES Index `picture_vector`
3. **搜索时**：用户上传/选择图片 → 调用阿里云百炼 API 生成查询向量 → 在 ES 中按 `spaceId` 过滤 → KNN 检索 Top 10

---

## 3. 数据模型

### 3.1 ES Index 设计

**Index Name: `picture_vector`**

```json
{
  "settings": {
    "index": {
      "number_of_shards": 1,
      "number_of_replicas": 0
    },
    "analysis": {
      "analyzer": {
        "ik_max_word": {
          "type": "standard"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "pictureId": {
        "type": "long"
      },
      "spaceId": {
        "type": "long"
      },
      "url": {
        "type": "keyword"
      },
      "thumbnailUrl": {
        "type": "keyword"
      },
      "name": {
        "type": "text"
      },
      "vector": {
        "type": "dense_vector",
        "dims": 512,
        "index": true,
        "similarity": "cosine"
      },
      "createTime": {
        "type": "date"
      }
    }
  }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| pictureId | long | 图片主键 ID |
| spaceId | long | 空间 ID，用于同空间内搜索过滤 |
| url | keyword | 原图 URL |
| thumbnailUrl | keyword | 缩略图 URL |
| name | text | 图片名称，支持分词搜索 |
| vector | dense_vector | 图片向量，512 维度，cosine 相似度 |
| createTime | date | 创建时间 |

### 3.2 领域实体

**PictureVectorRecord（领域实体）**
```java
public class PictureVectorRecord {
    private Long pictureId;
    private Long spaceId;
    private String url;
    private String thumbnailUrl;
    private String name;
    private float[] vector;
    private LocalDateTime createTime;
}
```

---

## 4. 接口设计

### 4.1 上传图片搜图

**Endpoint:** `POST /api/picture/search/vector/by-upload`

**Content-Type:** `multipart/form-data`

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | 是 | 图片文件 |
| spaceId | Long | 是 | 目标空间 ID |

**响应：**
```json
{
  "code": 0,
  "data": [
    {
      "pictureId": 456,
      "name": "风景照",
      "thumbnailUrl": "https://cos.tencent.com/thumb.jpg",
      "url": "https://cos.tencent.com/origin.jpg",
      "score": 0.95
    }
  ],
  "message": "ok"
}
```

### 4.2 选择已有图片搜图

**Endpoint:** `POST /api/picture/search/vector/by-picture`

**Content-Type:** `application/json`

**请求参数：**
```json
{
  "pictureId": 123,
  "spaceId": 1
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pictureId | Long | 是 | 已有的图片 ID |
| spaceId | Long | 是 | 目标空间 ID |

**响应：**
```json
{
  "code": 0,
  "data": [
    {
      "pictureId": 456,
      "name": "风景照",
      "thumbnailUrl": "https://cos.tencent.com/thumb.jpg",
      "url": "https://cos.tencent.com/origin.jpg",
      "score": 0.95
    }
  ],
  "message": "ok"
}
```

### 4.3 响应模型

**VectorSearchResultVO：**
```java
public class VectorSearchResultVO {
    private Long pictureId;       // 图片 ID
    private String name;          // 图片名称
    private String thumbnailUrl;   // 缩略图 URL
    private String url;           // 原图 URL
    private Double score;         // 相似度分值 (0-1)
}
```

---

## 5. 领域服务设计

### 5.1 PictureVectorService（领域服务接口）

```java
public interface PictureVectorService {
    /**
     * 将图片向量写入 ES
     * @param pictureId 图片 ID
     * @param spaceId 空间 ID
     * @param url 图片 URL
     * @param thumbnailUrl 缩略图 URL
     * @param name 图片名称
     */
    void savePictureVector(Long pictureId, Long spaceId, String url,
                           String thumbnailUrl, String name);

    /**
     * 通过图片 URL 提取向量并搜索相似图片
     * @param spaceId 空间 ID
     * @param imageUrl 图片 URL
     * @param topK 返回数量
     * @return 相似图片列表
     */
    List<PictureVectorRecord> searchSimilarPictures(Long spaceId, String imageUrl, int topK);

    /**
     * 通过已有图片 ID 搜索相似图片
     * @param spaceId 空间 ID
     * @param pictureId 已有图片 ID
     * @param topK 返回数量
     * @return 相似图片列表
     */
    List<PictureVectorRecord> searchSimilarPicturesById(Long spaceId, Long pictureId, int topK);

    /**
     * 删除图片向量
     * @param pictureId 图片 ID
     */
    void deletePictureVector(Long pictureId);
}
```

### 5.2 实现类 PictureVectorServiceImpl

关键逻辑：
1. 调用 `AliYunAiApi.extractImageVector(url)` 获取向量
2. 调用 `PictureVectorRepository.search(spaceId, vector, topK)` 执行搜索
3. 验证返回的 `pictureId` 在 MySQL 中存在且未删除

---

## 6. 基础设施设计

### 6.1 阿里云百炼多模态 Embedding API

**AliYunAiApi 扩展方法：**

```java
public class AliYunAiApi {
    /**
     * 提取图片向量（使用百炼多模态 Embedding API）
     * @param imageUrl 图片 URL（公网可访问）
     * @return float[] 向量数组
     */
    public float[] extractImageVector(String imageUrl);
}
```

**注意：**
- 需要使用可以公网访问的图片 URL，腾讯云 COS URL 默认支持公网访问
- 向量维度需在首次调用后验证，并在 ES Mapping 中保持一致

### 6.2 ES 仓储

**PictureVectorRepository：**

```java
public interface PictureVectorRepository {
    /**
     * 创建索引（启动时调用）
     */
    void createIndexIfNotExists();

    /**
     * 保存图片向量
     */
    void save(PictureVectorRecord record);

    /**
     * KNN 搜索
     * @param spaceId 空间 ID
     * @param queryVector 查询向量
     * @param topK 返回数量
     * @return 匹配的记录
     */
    List<PictureVectorRecord> search(Long spaceId, float[] queryVector, int topK);

    /**
     * 删除向量
     */
    void deleteByPictureId(Long pictureId);
}
```

---

## 7. 权限控制与安全校验

- 搜索接口需要用户已登录
- 使用 `@SaSpaceCheckPermission(PICTURE_VIEW)` 确保用户有空间的浏览权限
- 搜索结果只返回当前用户有权限访问的图片（同 spaceId 过滤在 ES 查询层面完成）

### 7.1 安全性校验

**by-pictureId 接口额外校验：**
- 验证 `pictureId` 对应的图片确实属于指定的 `spaceId`
- 如果图片不存在或不属于该空间，返回 `PARAMS_ERROR`

```java
// 在 searchSimilarPicturesById 中添加校验
Picture picture = pictureRepository.getById(pictureId);
ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
ThrowUtils.throwIf(!spaceId.equals(picture.getSpaceId()),
    ErrorCode.PARAMS_ERROR, "图片不在指定空间中");
```

**by-upload 接口额外校验：**
- 验证上传文件是图片类型（image/jpeg, image/png, image/jpg, image/bmp）
- 限制文件大小（建议 10MB 以内）

---

## 8. 前端改造（简要）

前端需要新增两个接口调用：

```typescript
// 按上传图片搜索
export const searchVectorByUpload = (file: File, spaceId: number) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('spaceId', spaceId.toString());
  return request.post('/api/picture/search/vector/by-upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

// 按已有图片搜索
export const searchVectorByPicture = (pictureId: number, spaceId: number) => {
  return request.post('/api/picture/search/vector/by-picture', {
    pictureId,
    spaceId
  });
};
```

---

## 9. 部署文档（由用户执行）

### 9.1 Docker 部署 ElasticSearch

```bash
# 创建网络
docker network create es-network

# 启动单节点 ES
docker run -d \
  --name elasticsearch \
  --net es-network \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0

# 验证
curl http://localhost:9200
```

### 9.2 application.yaml 配置

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200

# 阿里云百炼 API 配置
aliyun:
  dashscope:
    api-key: your-api-key-here
```

---

## 10. 待完成工作

1. 添加 `spring-boot-starter-data-elasticsearch` 依赖
2. 在 `AliYunAiApi` 中实现 `extractImageVector()` 方法
3. 实现 `PictureVectorRepository`
4. 实现 `PictureVectorService`
5. 实现 `PictureController` 两个搜索接口
6. 在图片上传流程中集成向量提取和存储
7. 前端两个搜索入口
8. 首次部署后验证向量维度是否与 ES Mapping 一致

---

## 11. 文件清单

**新增文件：**
- `domain/picture/entity/PictureVectorRecord.java` - 领域实体
- `domain/picture/service/PictureVectorService.java` - 领域服务接口
- `domain/picture/service/impl/PictureVectorServiceImpl.java` - 领域服务实现
- `infrastructure/repository/PictureVectorRepository.java` - ES 仓储接口
- `infrastructure/repository/impl/PictureVectorRepositoryImpl.java` - ES 仓储实现
- `interfaces/controller/PictureVectorController.java` - 控制器
- `interfaces/dto/picture/SearchVectorByPictureRequest.java` - 请求 DTO
- `interfaces/vo/picture/VectorSearchResultVO.java` - 响应 VO
- `infrastructure/api/aliyunai/AliYunAiApi.java` - 阿里云百炼 AI API（扩展）

**修改文件：**
- `pom.xml` - 添加 ES 依赖
- `application.yaml` - 添加 ES 配置
- `PictureController.java` - （可选）集成向量提取到上传流程
