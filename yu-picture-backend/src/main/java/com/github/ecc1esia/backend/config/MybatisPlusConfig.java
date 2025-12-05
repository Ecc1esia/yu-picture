package com.github.ecc1esia.backend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 用于配置 MyBatis-Plus 相关的拦截器和其他扩展功能
 */
@Configuration
@MapperScan("com.github.ecc1esia.picture.infrastructure.dao")
public class MybatisPlusConfig {

    /**
     * 创建并配置 MyBatis-Plus 拦截器
     * 主要用于添加内置的分页拦截器，以支持分页查询功能
     *
     * @return 配置好的 MybatisPlusInterceptor 实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加分页拦截器，指定数据库类型为 MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
