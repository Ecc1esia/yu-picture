# 图片向量检索功能 - 部署指南

## 一、环境准备

### 1.1 部署 ElasticSearch（单机 Docker）

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

### 1.2 申请阿里云百炼 API Key

1. 访问阿里云百炼：https://www.aliyun.com/product/dashscope
2. 开通百炼大模型服务
3. 在控制台创建 API Key

---

## 二、配置

### 2.1 修改 application.yaml

文件位置：`yu-picture-ddd/src/main/resources/application.yaml`

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200

# 阿里云百炼 API 配置
aliYunAi:
  apiKey: your-api-key-here  # 替换为实际的 API Key
```

---

## 三、代码修改记录

### 3.1 新增文件清单

| 文件路径 | 说明 |
|----------|------|
| `domain/picture/entity/PictureVectorRecord.java` | 向量记录实体 |
| `domain/picture/service/PictureVectorService.java` | 领域服务接口 |
| `domain/picture/service/impl/PictureVectorServiceImpl.java` | 领域服务实现 |
| `infrastructure/repository/PictureVectorRepository.java` | ES 仓储接口 |
| `infrastructure/repository/impl/PictureVectorRepositoryImpl.java` | ES 仓储实现 |
| `interfaces/controller/PictureVectorController.java` | 控制器 |
| `interfaces/dto/picture/SearchVectorByPictureRequest.java` | 请求 DTO |
| `interfaces/vo/picture/VectorSearchResultVO.java` | 响应 VO |

### 3.2 修改文件清单

| 文件路径 | 修改内容 |
|----------|----------|
| `pom.xml` | 添加 `spring-boot-starter-data-elasticsearch` 依赖 |
| `application.yaml` | 添加 ES 和百炼配置 |
| `AliYunAiApi.java` | 添加 `extractImageVector()` 方法 |
| `PictureDomainServiceImpl.java` | 注入 `PictureVectorService`，集成向量提取 |
| `yu-picture-web/src/api/pictureController.ts` | 添加向量搜索 API 调用函数 |

---

## 四、API 接口

### 4.1 上传图片搜图

**接口：** `POST /api/picture/search/vector/by-upload`

**Content-Type：** `multipart/form-data`

**参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 图片文件 |
| spaceId | Long | 是 | 空间 ID |

**响应：**
```json
{
  "code": 0,
  "data": [
    {
      "pictureId": 456,
      "name": "风景照",
      "thumbnailUrl": "https://...",
      "url": "https://...",
      "score": 0.95
    }
  ],
  "message": "ok"
}
```

### 4.2 选择已有图片搜图

**接口：** `POST /api/picture/search/vector/by-picture`

**Content-Type：** `application/json`

**参数：**
```json
{
  "pictureId": 123,
  "spaceId": 1
}
```

**响应：**
```json
{
  "code": 0,
  "data": [
    {
      "pictureId": 456,
      "name": "风景照",
      "thumbnailUrl": "https://...",
      "url": "https://...",
      "score": 0.95
    }
  ],
  "message": "ok"
}
```

---

## 五、待完成事项

### 5.1 验证向量维度

首次部署后，需要验证阿里云百炼 API 返回的实际向量维度：

1. 启动应用，上传一张图片
2. 检查日志输出：`成功提取图片向量, imageUrl=xxx, dimension=xxx`
3. 如果维度不是 512，修改 `PictureVectorRepositoryImpl.java` 中的 `VECTOR_DIMENSION` 常量

### 5.2 实现 by-upload 的 COS 上传

当前 `PictureVectorController.searchByUpload()` 中的 COS 上传是占位符，需要接入实际逻辑：

```java
// 当前代码（占位符）
String tempImageUrl = "https://example.com/temp/" + UUID.randomUUID() + ".jpg";

// 需要实现的逻辑
// 1. 将 MultipartFile 上传到腾讯云 COS
// 2. 获取 COS 公网访问 URL
// 3. 使用该 URL 调用向量提取 API
```

### 5.3 集成向量提取到图片上传

当前 `PictureDomainServiceImpl` 中已将 `PictureVectorService` 注入，但 `savePictureVector` 调用被注释：

```java
// PictureDomainServiceImpl.java 中
// pictureVectorService.savePictureVector(picture); // TODO: uncomment after upload is working
```

在图片上传功能完善后，取消注释即可。

---

## 六、ES 索引信息

**Index Name：** `picture_vector`

**Mapping：**
```json
{
  "properties": {
    "pictureId": { "type": "long" },
    "spaceId": { "type": "long" },
    "url": { "type": "keyword" },
    "thumbnailUrl": { "type": "keyword" },
    "name": { "type": "text" },
    "vector": { "type": "dense_vector", "dims": 512, "index": true, "similarity": "cosine" },
    "createTime": { "type": "date" }
  }
}
```

索引会在应用启动时自动创建（如果不存在）。

---

## 七、常见问题

### 7.1 向量提取失败

检查：
1. 阿里云百炼 API Key 是否正确配置
2. 图片 URL 是否公网可访问（腾讯云 COS 默认支持）
3. 查看应用日志中的错误信息

### 7.2 ES 连接失败

检查：
1. Docker 中的 ES 是否正常运行：`docker ps`
2. ES 是否能正常访问：`curl http://localhost:9200`
3. application.yaml 中的 ES 地址是否正确

### 7.3 搜索结果为空

可能原因：
1. 空间中还没有图片向量数据（需要先上传图片）
2. 图片向量维度与 ES Mapping 不一致
3. 向量提取失败，查看日志排查
