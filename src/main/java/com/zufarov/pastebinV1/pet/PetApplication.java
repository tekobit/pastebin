package com.zufarov.pastebinV1.pet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
// just testing git 2
@EnableFeignClients
@SpringBootApplication
public class PetApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetApplication.class, args);
    }

}
