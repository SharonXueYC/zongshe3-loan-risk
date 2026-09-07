package com.example.zongshe1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 综合贷款风控系统主启动类
 * 使用Spring Boot 3.2.5和JDK 21
 */
@SpringBootApplication
@EnableTransactionManagement  // 启用声明式事务
@EnableScheduling             // 启用定时任务
public class Zongshe1Application {

    public static void main(String[] args) {
        SpringApplication.run(Zongshe1Application.class, args);
        System.out.println("综合贷款风控系统启动成功！");
        System.out.println("Swagger文档地址: http://localhost:8080/api/swagger-ui.html");
        System.out.println("API文档地址: http://localhost:8080/api/api-docs");
    }
}