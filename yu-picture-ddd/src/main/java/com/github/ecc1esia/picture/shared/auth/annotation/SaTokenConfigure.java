package com.github.ecc1esia.picture.shared.auth.annotation;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;


/**
 * Sa-Token 开启注解和配置
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }

    @PostConstruct
    public void rewriteSaStrategy() {
        SaAnnotationStrategy.instance.getAnnotation = (element, annotationClass) -> {
            // 重写Sa-Token的注解处理器，增加注解合并功能
            return AnnotatedElementUtils.getMergedAnnotation(element, annotationClass);
        };
    }
}
