package com.financaspro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FinancasproApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancasproApplication.class, args);
    }
}
