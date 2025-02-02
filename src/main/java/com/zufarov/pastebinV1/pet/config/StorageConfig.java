package com.zufarov.pastebinV1.pet.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    public AmazonS3 generateS3Client() {
        return AmazonS3ClientBuilder.standard().withEndpointConfiguration(
                new AmazonS3ClientBuilder.EndpointConfiguration(
                        "storage.yandexcloud.net", "ru-central1"
                )
        ).build();


    }
}
