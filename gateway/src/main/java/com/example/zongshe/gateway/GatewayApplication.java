package com.example.zongshe.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关独立进程入口。和 backend 的 Zongshe1Application 是两个 main，
 * 启动后是两个 JVM、两个端口，这才叫「单独可运行的程序」。
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
