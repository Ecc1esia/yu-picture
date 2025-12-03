package com.github.ecc1esia.backend.manager.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

/**
 * WebSocket 配置类，用于定义WebSocket的连接配置
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    /**
     * 处理图片编辑的WebSocket处理器
     */
    @Resource
    private PictureEditHandler pictureEditHandler;

    /**
     * WebSocket握手拦截器，用于在握手过程中添加额外的逻辑
     */
    @Resource
    private WsHandshakeInterceptor wsHandshakeInterceptor;

    /**
     * 注册WebSocket处理器和拦截器
     *
     * @param registry WebSocket处理器注册对象，用于配置WebSocket处理器和拦截器
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册图片编辑的WebSocket处理器，指定其路径，并添加握手拦截器，允许所有来源的连接
        registry.addHandler(pictureEditHandler, "/ws/picture/edit")
                .addInterceptors(wsHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
