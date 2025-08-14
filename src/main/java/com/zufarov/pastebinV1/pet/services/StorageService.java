// Used to save paste content to cloud storage(yandex cloud storage, using aws tools)

package com.zufarov.pastebinV1.pet.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.zufarov.pastebinV1.pet.dtos.PasteRequestDto;
import com.zufarov.pastebinV1.pet.dtos.PasteUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class StorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    private final AmazonS3 s3Client;
    private final CacheService cacheService;


    public StorageService(AmazonS3 s3Client, CacheService cacheService) {
        this.s3Client = s3Client;
        this.cacheService = cacheService;
    }


    public String uploadPasteToStorage(PasteRequestDto paste, String fileName) {
        s3Client.putObject(bucketName, fileName, paste.getContent());
        cacheService.putPasteContentToCache(paste.getContent(),fileName);
        return String.valueOf(s3Client.getUrl(bucketName,fileName));
    }

    @Cacheable(value = "pasteContentCache")
    public String getPasteFromStorage(String pasteId) throws IOException {
        S3Object s3Object = s3Client.getObject(bucketName, pasteId);
        return IOUtils.toString(s3Object.getObjectContent());
    }

    @CacheEvict(value = "pasteContentCache")
    public void deletePasteFromStorage(String pasteId) {
        s3Client.deleteObject(bucketName,pasteId);
    }

    public void updatePasteInStorage(PasteUpdateDto paste, String id) {
        s3Client.putObject(bucketName,id,paste.getContent());
        cacheService.putPasteContentToCache(paste.getContent(),id);
    }

}
