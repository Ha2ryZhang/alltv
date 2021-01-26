package com.debugers.alltv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AlltvApplication {

    public static void main(String[] args) {
        System.setProperty("spring.config.additional-location",
                "file:${user.home}/.alltv/");
        System.out.println(System.getProperty("spring.config.additional-location"));
        SpringApplication.run(AlltvApplication.class, args);
    }

}
