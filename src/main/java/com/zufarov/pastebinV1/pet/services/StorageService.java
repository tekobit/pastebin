// Used to save paste content to cloud storage(yandex cloud storage, using aws tools)

package com.zufarov.pastebinV1.pet.services;


import com.zufarov.pastebinV1.pet.dtos.PasteRequestDto;
import com.zufarov.pastebinV1.pet.dtos.PasteUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Value("${application.bucket.prefix}")
    private String bucketPrefix;

    private final S3Client s3Client;
    private final CacheService cacheService;

    public String uploadPasteToStorage(PasteRequestDto paste, String fileName) {
        String objectKey = bucketPrefix + fileName;

        s3Client.putObject(req -> req
                .bucket(bucketName)
                .key(objectKey),
                RequestBody.fromString(paste.content())
        );
        cacheService.putPasteContentToCache(paste.content(),fileName);
        return String.valueOf(s3Client.utilities().getUrl(req -> req
                .bucket(bucketName)
                .key(objectKey))
        );

    }

    @Cacheable(value = "pasteContentCache")
    public String getPasteFromStorage(String pasteId) throws IOException {
        String objectKey = bucketPrefix + pasteId;

        ResponseInputStream<GetObjectResponse> response = s3Client.getObject(req -> req
                .bucket(bucketName)
                .key(objectKey)
        );

        try (response) {
            return new String(response.readAllBytes());
        }
    }

    @CacheEvict(value = "pasteContentCache")
    public void deletePasteFromStorage(String pasteId) {
        String objectKey = bucketPrefix + pasteId;

        s3Client.deleteObject(req -> {
            req.bucket(bucketName)
            .key(objectKey);
        });
    }

    public void updatePasteInStorage(PasteUpdateDto paste, String id) {
        String objectKey = bucketPrefix + id;

        s3Client.putObject(req -> req
                .bucket(bucketName)
                .key(objectKey),
                RequestBody.fromString(paste.content())
        );

        cacheService.putPasteContentToCache(paste.content(),id);
    }

}
