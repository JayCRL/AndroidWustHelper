package com.example.wustchat;

import cn.wustlinghang.mywust.common.security.JwtUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@EnableDiscoveryClient
@Import(JwtUtils.class)
@SpringBootApplication
public class WustchatApplication {

    public static void main(String[] args) {
        SpringApplication.run(WustchatApplication.class, args);
    }
}
