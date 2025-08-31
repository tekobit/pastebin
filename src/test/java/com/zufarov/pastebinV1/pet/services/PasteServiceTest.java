package com.zufarov.pastebinV1.pet.services;

import com.zufarov.pastebinV1.TestDataFactory;
import com.zufarov.pastebinV1.pet.dtos.PasteRequestDto;
import com.zufarov.pastebinV1.pet.dtos.PasteResponseDto;
import com.zufarov.pastebinV1.pet.dtos.PasteUpdateDto;
import com.zufarov.pastebinV1.pet.mappers.PasteMapper;
import com.zufarov.pastebinV1.pet.models.Paste;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasteServiceTest {
    @Mock
    private TokenService tokenService;

    @Mock
    private DataBaseService dataBaseService;

    @Mock
    private StorageService storageService;

    @Mock
    private PasteMapper pasteMapper;

    @InjectMocks
    private PasteService pasteService;

    @Test
    void getPaste_ShouldReturnPasteResponseDto_WhenPasteExists() throws IOException {
        String pasteContent = "pasteContent";
        String pasteId = "pasteId";

        Paste pasteMetadata =  new Paste();
        pasteMetadata.setId(pasteId);

        PasteResponseDto expectedDto = TestDataFactory.createDefaultPasteResponseDto();

        when(dataBaseService.getPasteMetadata(pasteId)).thenReturn(pasteMetadata);
        when(storageService.getPasteFromStorage(pasteId)).thenReturn(pasteContent);
        when(pasteMapper.toPasteResponseDto(pasteMetadata,pasteContent)).thenReturn(expectedDto);

        PasteResponseDto actualDto = pasteService.getPaste(pasteId);

        assertEquals(expectedDto,actualDto);

        verify(dataBaseService,times(1)).getPasteMetadata(pasteId);
        verify(storageService,times(1)).getPasteFromStorage(pasteId);
        verify(pasteMapper,times(1)).toPasteResponseDto(pasteMetadata,pasteContent);

    }

    @Test
    void getPaste_ShouldReturnNull_WhenStorageServiceThrowsIOException() throws IOException {
        String pasteId = "pasteId";
        Paste pasteMetadata =  new Paste();
        pasteMetadata.setId(pasteId);

        when(dataBaseService.getPasteMetadata(pasteId)).thenReturn(pasteMetadata);
        when(storageService.getPasteFromStorage(pasteId)).thenThrow(new IOException());

        PasteResponseDto actualDto = pasteService.getPaste(pasteId);
        assertNull(actualDto);

        verify(dataBaseService,times(1)).getPasteMetadata(pasteId);
        verify(storageService,times(1)).getPasteFromStorage(pasteId);
        verify(pasteMapper,never()).toPasteResponseDto(any(),any());
    }

    @Test
    void uploadPaste() {
        PasteRequestDto pasteRequestDto = TestDataFactory.createDefaultPasteRequestDto();

        String fileName = "unique_id";
        String pasteUrl = "paste_url";

        when(tokenService.getUniqueId()).thenReturn(fileName);
        when(storageService.uploadPasteToStorage(pasteRequestDto,fileName)).thenReturn(pasteUrl);

        String result = pasteService.uploadPaste(pasteRequestDto);

        assertTrue(result.contains(fileName));

        verify(tokenService,times(1)).getUniqueId();
        verify(storageService,times(1)).uploadPasteToStorage(pasteRequestDto,fileName);
        verify(dataBaseService,times(1)).savePasteMetadata(pasteRequestDto,fileName,pasteUrl);
    }

    @Test
    void deletePaste() {
        String pasteId = "pasteId";

        String result =  pasteService.deletePaste(pasteId);

        assertTrue(result.contains(pasteId));

        verify(dataBaseService,times(1)).deletePasteMetadata(pasteId);
        verify(storageService,times(1)).deletePasteFromStorage(pasteId);
    }

    @Test
    void updatePaste() {
        PasteUpdateDto pasteUpdateDto = TestDataFactory.createDefaultPasteUpdateDto();
        String pasteId = "pasteId";

        String result = pasteService.updatePaste(pasteUpdateDto,pasteId);

        assertTrue(result.contains(pasteId));

        verify(dataBaseService,times(1)).updatePasteMetadata(pasteUpdateDto,pasteId);
        verify(storageService,times(1)).updatePasteInStorage(pasteUpdateDto,pasteId);

    }
}