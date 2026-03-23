package com.github.ecc1esia.picture.infrastructure.repository.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.github.ecc1esia.picture.domain.picture.entity.PictureVectorRecord;
import com.github.ecc1esia.picture.infrastructure.repository.PictureVectorRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.IOException;
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
                                .properties("url", p -> p.keyword(k -> k))
                                .properties("thumbnailUrl", p -> p.keyword(k -> k))
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
            // 将 float[] 转换为 List<Float>（ES Java Client 需要）
            List<Float> vectorList = new ArrayList<>(queryVector.length);
            for (float v : queryVector) {
                vectorList.add(v);
            }

            // ES 8.x KNN搜索
            SearchResponse<PictureVectorRecord> response = elasticsearchClient.search(SearchRequest.of(s -> s
                            .index(INDEX_NAME)
                            .knn(k -> k
                                    .field("vector")
                                    .queryVector(vectorList)
                                    .k(topK)
                                    .numCandidates(topK * 2)
                            )
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
                    // 过滤 spaceId
                    if (spaceId.equals(record.getSpaceId())) {
                        results.add(record);
                    }
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
