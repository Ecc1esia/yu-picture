# AI 向量图片检索功能设计文档

## 一、设计思路

### 1.1 核心目标
实现"同一空间内图片向量检索"功能：用户可以通过上传图片或选择已有图片，在同一空间内搜索相似图片。

### 1.2 技术选型
- **向量提取**：阿里云百炼多模态 Embedding API (`qwen-vl-embedding`)
- **向量存储**：ElasticSearch 8.x (KNN 搜索)
- **相似度算法**：余弦相似度 (cosine)

### 1.3 整体流程

```
用户上传图片/选择已有图片
         ↓
提取图片向量 (阿里云百炼 API)
         ↓
存入 ElasticSearch
         ↓
KNN 相似度搜索
         ↓
返回相似图片列表
```

---

## 二、架构设计

### 2.1 模块结构 (DDD 分层)

```
interfaces (controllers, DTOs, VOs) → application (services) → domain (entities, services, repositories) → infrastructure (mappers, configs, api)
```

### 2.2 核心领域模块

| 模块 | 类名 | 职责 |
|------|------|------|
| Domain | `PictureVectorRecord` | ES 文档实体，包含图片信息和向量 |
| Domain | `PictureVectorService` | 领域服务接口，定义向量操作 |
| Infrastructure | `PictureVectorRepository` | ES 仓储接口及实现 |
| Infrastructure | `AliYunAiApi` | 阿里云百炼 API 调用 |
| Interfaces | `PictureVectorController` | 向量搜索 REST API |

### 2.3 向量维度
- 默认 512 维（需与阿里云百炼 API 返回维度一致）

---

## 三、功能详细

### 3.1 API 接口

#### 3.1.1 上传图片搜图
```
POST /api/picture/search/vector/by-upload
Content-Type: multipart/form-data

参数:
- file: 图片文件
- spaceId: 空间 ID

响应:
{
  "code": 0,
  "data": [
    {
      "pictureId": 1,
      "name": "风景照.jpg",
      "thumbnailUrl": "https://...",
      "url": "https://...",
      "score": 0.95
    }
  ]
}
```

#### 3.1.2 选择已有图片搜图
```
POST /api/picture/search/vector/by-picture
Content-Type: application/json

参数:
{
  "pictureId": 123,    // 已有图片 ID
  "spaceId": 1         // 空间 ID
}

响应: 同上
```

### 3.2 领域服务

```java
public interface PictureVectorService {
    // 保存图片向量到 ES
    void savePictureVector(Picture picture);

    // 通过 URL 搜索相似图片
    List<PictureVectorRecord> searchSimilarPictures(Long spaceId, String imageUrl, int topK);

    // 通过已有图片 ID 搜索相似图片
    List<PictureVectorRecord> searchSimilarPicturesById(Long spaceId, Long pictureId, int topK);

    // 删除图片向量
    void deletePictureVector(Long pictureId);
}
```

### 3.3 ES 索引结构

索引名：`picture_vector`

```json
{
  "mappings": {
    "properties": {
      "pictureId": { "type": "long" },
      "spaceId": { "type": "long" },
      "url": { "type": "keyword" },
      "thumbnailUrl": { "type": "keyword" },
      "name": { "type": "text" },
      "vector": {
        "type": "dense_vector",
        "dims": 512,
        "index": true,
        "similarity": "cosine"
      },
      "createTime": { "type": "date" }
    }
  }
}
```

---

## 四、代码结构

```
yu-picture-ddd/src/main/java/com/github/ecc1esia/picture/
├── domain/picture/
│   ├── entity/
│   │   └── PictureVectorRecord.java          # ES 文档实体
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
│       └── AliYunAiApi.java                   # 阿里云百炼 API (已扩展)
├── interfaces/
│   ├── controller/
│   │   └── PictureVectorController.java       # 向量搜索控制器
│   ├── dto/picture/
│   │   └── SearchVectorByPictureRequest.java  # by-picture 请求 DTO
│   └── vo/picture/
│       └── VectorSearchResultVO.java          # 搜索结果 VO
```

---

## 五、前端调用

### 5.1 API 函数

```typescript
// 上传图片搜图
import { searchVectorByUpload } from '@/api/pictureController'
const results = await searchVectorByUpload(file, spaceId)

// 选择已有图片搜图
import { searchVectorByPicture } from '@/api/pictureController'
const results = await searchVectorByPicture(pictureId, spaceId)
```

### 5.2 组件
- `VectorSearchResult.vue` - 展示搜索结果，包含相似度百分比

---

## 六、运行测试

### 6.1 前置条件

1. **启动 ElasticSearch 8.x**
```bash
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  elasticsearch:8.11.0
```

2. **配置阿里云百炼 API Key**
   - 在 `application.yaml` 中配置：
```yaml
aliYunAi:
  apiKey: your-api-key-here
```

3. **配置腾讯云 COS**
```yaml
cos:
  client:
    host: https://your-bucket.cos.xxx.myqcloud.com
    secretId: your-secret-id
    secretKey: your-secret-key
    region: ap-xxx
    bucket: your-bucket
```

### 6.2 启动后端

```bash
cd D:/workspace/project/yu-picture-backup/.worktrees/picture-vector-search/yu-picture-ddd

# 安装依赖 (如需要)
mvn install

# 运行
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端启动后运行在：`http://localhost:8123/api`

### 6.3 启动前端

```bash
cd D:/workspace/project/yu-picture-backup/.worktrees/picture-vector-search/yu-picture-web

# 安装依赖
npm install

# 运行开发服务器
npm run dev
```

前端启动后运行在：`http://localhost:5173`

### 6.4 测试步骤

#### 6.4.1 验证 ES 索引创建

启动后端后，应用会自动创建 `picture_vector` 索引。

验证：
```bash
curl http://localhost:9200/picture_vector
```

#### 6.4.2 测试上传图片搜图

```bash
# 替换为实际值
curl -X POST "http://localhost:8123/api/picture/search/vector/by-upload" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/your/image.jpg" \
  -F "spaceId=1"
```

#### 6.4.3 测试选择已有图片搜图

```bash
curl -X POST "http://localhost:8123/api/picture/search/vector/by-picture" \
  -H "Content-Type: application/json" \
  -d '{"pictureId": 123, "spaceId": 1}'
```

#### 6.4.4 前端测试

1. 打开前端页面，进入某个空间
2. 上传几张测试图片（会提取向量存入 ES）
3. 选择一张图片，点击"以图搜图"按钮
4. 应该能看到相似图片列表

---

## 七、注意事项

1. **向量维度**：需在首次调用后确认阿里云百炼 API 返回的实际维度，目前代码中假设为 512 维

2. **COS URL**：by-upload 接口需要公网可访问的图片 URL，腾讯云 COS 默认支持

3. **错误处理**：向量提取失败不影响主流程（图片仍可上传），仅记录日志

4. **性能**：向量提取依赖外部 API，可能有延迟，建议异步处理

---

## 八、集成时机

当前图片上传流程 (`PictureDomainServiceImpl.uploadPicture`) 尚未完整实现。向量提取集成将在上传流程完成后启用。集成代码已预留：

```java
// 在 uploadPicture 成功后调用
savePictureVectorAsync(picture);
```
