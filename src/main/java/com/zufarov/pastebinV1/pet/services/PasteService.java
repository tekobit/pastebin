package com.zufarov.pastebinV1.pet.services;

import com.zufarov.pastebinV1.pet.dtos.PasteRequestDto;
import com.zufarov.pastebinV1.pet.dtos.PasteResponseDto;
import com.zufarov.pastebinV1.pet.dtos.PasteUpdateDto;
import com.zufarov.pastebinV1.pet.mappers.PasteMapper;
import com.zufarov.pastebinV1.pet.models.Paste;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasteService {

    private final TokenService tokenService;
    private final DataBaseService dataBaseService;
    private final StorageService storageService;
    private final PasteMapper pasteMapper;

    public PasteResponseDto getPaste(String pasteId) {
        Paste pasteMetadata = dataBaseService.getPasteMetadata(pasteId);
        try {
            String pasteContent = storageService.getPasteFromStorage(pasteId);

            return pasteMapper.toPasteResponseDto(pasteMetadata,pasteContent);
        } catch (IOException e) {
            log.error("error during getting paste content");
        }
        return null;
    }

    public String uploadPaste(PasteRequestDto paste) {
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

    public String updatePaste(PasteUpdateDto paste, String id) {
        dataBaseService.updatePasteMetadata(paste,id);
        storageService.updatePasteInStorage(paste,id);
        return id + " was successfully updated";
    }
}
