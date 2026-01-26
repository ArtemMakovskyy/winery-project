package com.winestoreapp.wineryadminui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.winestoreapp.wineryadminui")
public class WineryAdminUiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WineryAdminUiApplication.class, args);
    }

}
