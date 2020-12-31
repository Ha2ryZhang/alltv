package com.debugers.alltv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AlltvApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlltvApplication.class, args);
    }

}
