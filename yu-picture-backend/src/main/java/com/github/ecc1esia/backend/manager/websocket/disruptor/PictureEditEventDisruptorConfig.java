package com.github.ecc1esia.backend.manager.websocket.disruptor;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 图片编辑事件 Disruptor 配置类
 * 该配置类用于初始化和配置Disruptor，以处理图片编辑事件
 */
@Configuration
public class PictureEditEventDisruptorConfig {

    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;

    /**
     * 创建并初始化处理图片编辑事件的Disruptor
     *
     * @return 初始化后的Disruptor对象
     */
    @Bean("pictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> messageModelRingBuffer() {
        // 定义环形缓冲区的大小
        int bufferSize = 1024 * 256;

        // 创建Disruptor实例，使用指定的事件工厂、缓冲区大小和线程工厂
        Disruptor<PictureEditEvent> disruptor = new Disruptor<PictureEditEvent>(
                PictureEditEvent::new,
                bufferSize,
                ThreadFactoryBuilder.create().setNamePrefix("pictureEditEventDisruptor").build()
        );
        // 设置消费者
        disruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandler);
        // 启动 disruptor
        disruptor.start();
        return disruptor;
    }
}
