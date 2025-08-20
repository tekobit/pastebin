package com.zufarov.pastebinV1.pet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.endpoints.EndpointProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class StorageConfig {

    @Value("${cloud.aws.endpoint-url}")
    private String endpoint;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public S3Client generateS3Client() throws URISyntaxException {
        Region regionS3 = Region.of(region);
        URI endpoinUri = new URI(endpoint);

        return S3Client.builder()
                .region(regionS3)
                .endpointOverride(endpoinUri)
                .build();

    }
}
