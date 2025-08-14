package com.zufarov.pastebinV1.pet.services;

import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.RequestModels.CreateRequestPaste;
import com.zufarov.pastebinV1.pet.models.RequestModels.RequestPaste;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class PasteService {

    private final TokenService tokenService;
    private final DataBaseService dataBaseService;
    private final StorageService storageService;

    public PasteService(TokenService tokenService, DataBaseService dataBaseService, StorageService storageService) {
        this.tokenService = tokenService;
        this.dataBaseService = dataBaseService;
        this.storageService = storageService;
    }

    public RequestPaste getPaste(String pasteId) {
        Paste pasteMetadata = dataBaseService.getPasteMetadata(pasteId);
        try {
            String pasteContent = storageService.getPasteFromStorage(pasteId);

            RequestPaste requestPaste = new RequestPaste();

            requestPaste.setContent(pasteContent);
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

    public String uploadPaste(CreateRequestPaste paste) {
        String fileName = tokenService.getUniqueId();
        String pasteURL = storageService.uploadPasteToStorage(paste,fileName);
        dataBaseService.savePasteMetadata(paste,fileName,pasteURL);
        return fileName + " was successfully saved!";
    }

    public String deletePaste(String pasteId) {
        dataBaseService.deletePasteMetadata(pasteId);
        storageService.deletePasteFromStorage(pasteId);
        return pasteId + " was successfully deleted";
    }

    public String updatePaste(RequestPaste paste) {
        dataBaseService.updatePasteMetadata(paste);
        storageService.updatePasteInStorage(paste);
        return paste.getId() + " was successfully updated";
    }
}
