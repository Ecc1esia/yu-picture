package com.github.ecc1esia.picture.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * JsonConfig 类用于配置 Jackson ObjectMapper，以全局方式配置 JSON 处理方式
 */
@JsonComponent
public class JsonConfig {

    /**
     * 创建并配置 ObjectMapper 实例
     *
     * @param builder Jackson2ObjectMapperBuilder 构建器，用于创建 ObjectMapper 实例
     * @return 返回配置好的 ObjectMapper 实例
     */
    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        // 创建 ObjectMapper 实例，不启用 XML 映射
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // 创建一个 SimpleModule 实例，用于注册自定义的序列化器和反序列化器
        SimpleModule module = new SimpleModule();

        // 为 Long 类型添加自定义序列化器，将 Long 类型的值转换为字符串
        module.addSerializer(Long.class, ToStringSerializer.instance);

        // 为 long 基本数据类型添加自定义序列化器，将 long 类型的值转换为字符串
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);

        // 将自定义模块注册到 ObjectMapper 中
        objectMapper.registerModule(module);

        // 返回配置好的 ObjectMapper 实例
        return objectMapper;
    }
}

