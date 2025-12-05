package com.github.ecc1esia.backend.manager.sharding;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * 图片分片算法类，实现了 StandardShardingAlgorithm 接口
 * 用于动态分配数据到不同的表中，以实现数据分片
 */
public class PictureShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    /**
     * 执行精确分片
     * 根据 spaceId 动态生成分表名，如果生成的表名存在于可用的表名集合中，则返回该表名，否则返回逻辑表名
     *
     * @param availableTargetNames 可用的表名集合
     * @param preciseShardingValue 精确分片值，包含 spaceId 和逻辑表名
     * @return 分片后的表名
     */
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> preciseShardingValue) {
        Long spaceId = preciseShardingValue.getValue();
        String logicTableName = preciseShardingValue.getLogicTableName();
        // spaceId 为 null 表示查询所有图片
        if (spaceId == null) {
            return logicTableName;
        }
        // 根据 spaceId 动态生成分表名
        String realTableName = "picture_" + spaceId;
        if (availableTargetNames.contains(realTableName)) {
            return realTableName;
        } else {
            return logicTableName;
        }
    }

    /**
     * 执行范围分片
     * 目前未使用，直接返回空集合
     *
     * @param collection 可用的表名集合
     * @param rangeShardingValue 范围分片值
     * @return 分片后的表名集合
     */
    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Long> rangeShardingValue) {
        return new ArrayList<>();
    }

    /**
     * 获取属性
     * 目前未使用，直接返回 null
     *
     * @return 属性集合
     */
    @Override
    public Properties getProps() {
        return null;
    }

    /**
     * 初始化方法
     * 目前未使用
     *
     * @param properties 属性集合
     */
    @Override
    public void init(Properties properties) {
    }
}
