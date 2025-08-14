package com.zufarov.pastebinV1.pet.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Value("${cloud.aws.endpoint-url}")
    private String endpoint;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public AmazonS3 generateS3Client() {
        return AmazonS3ClientBuilder.standard().withEndpointConfiguration(
                new AmazonS3ClientBuilder.EndpointConfiguration(
                        endpoint, region
                )

        ).build();

    }
}
