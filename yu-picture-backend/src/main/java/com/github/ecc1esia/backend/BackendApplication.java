package com.github.ecc1esia.backend;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class})
//@EnableAsync
//@MapperScan("com.yupi.yupicture.infrastructure.mapper")
//@EnableAspectJAutoProxy(exposeProxy = true)
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
