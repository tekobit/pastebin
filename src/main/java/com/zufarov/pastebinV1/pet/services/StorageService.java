// Used to save paste content to cloud storage(yandex cloud storage, using aws tools)

package com.zufarov.pastebinV1.pet.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.RequestModels.RequestPaste;
import com.zufarov.pastebinV1.pet.models.RequestModels.CreateRequestPaste;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class StorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    private final AmazonS3 s3Client;
    private final TokenService tokenService;
    private final DataBaseService dataBaseService;

    public StorageService(AmazonS3 s3Client, TokenService tokenService, DataBaseService dataBaseService) {
        this.s3Client = s3Client;
        this.tokenService = tokenService;
        this.dataBaseService = dataBaseService;
    }

    public String uploadPaste(CreateRequestPaste paste) {
        String fileName = tokenService.getUniqueId();
        s3Client.putObject(bucketName, fileName, paste.getContent());
        String pasteURL = String.valueOf(s3Client.getUrl(bucketName,fileName));
        dataBaseService.savePasteMetadata(paste,fileName,pasteURL);
        return fileName + "was successfully saved";
    }

    public RequestPaste getPasteFromStorage(String pasteId) {
        Paste pasteMetadata = dataBaseService.getPasteMetadata(pasteId);
        S3Object s3Object = s3Client.getObject(bucketName, pasteId);
        S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
        try {
            RequestPaste requestPaste = new RequestPaste();
            requestPaste.setContent(IOUtils.toString(s3ObjectInputStream));
            requestPaste.setId(pasteMetadata.getId());
            requestPaste.setCreatedAt(pasteMetadata.getCreatedAt());
            requestPaste.setExpiresAt(pasteMetadata.getExpiresAt());
            requestPaste.setTitle(pasteMetadata.getTitle());
            requestPaste.setVisibility(pasteMetadata.getVisibility());
            requestPaste.setUserId(pasteMetadata.getOwner().getId());
            return requestPaste;
        } catch (IOException e) {
            log.error("error during getting paste content");
        }
        return null;
    }

    public String deletePasteFromStorage(String pasteID) {
        dataBaseService.deletePasteMetadata(pasteID);
        s3Client.deleteObject(bucketName,pasteID);
        return pasteID + " was successfully deleted";
    }

    public String updatePasteInStorage(RequestPaste paste) {
        dataBaseService.updatePasteMetadata(paste);
        s3Client.putObject(bucketName,paste.getId(),paste.getContent());
        return paste.getId() + " was successfully updated";
    }

}
