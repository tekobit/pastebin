package com.zufarov.pastebinV1.pet.controllers;

import com.zufarov.pastebinV1.pet.models.RequestModels.RequestPaste;
import com.zufarov.pastebinV1.pet.models.RequestModels.CreateRequestPaste;
import com.zufarov.pastebinV1.pet.models.RequestModels.RequestPermission;
import com.zufarov.pastebinV1.pet.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PasteController {
    private final StorageService storageService;

    @Autowired
    public PasteController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/createPaste")
    public ResponseEntity<CreateRequestPaste> createPaste() {
        return new ResponseEntity<>(new CreateRequestPaste(),HttpStatus.OK);
    }

    @PostMapping("/createPaste")
    public ResponseEntity<String> createPaste(@RequestBody CreateRequestPaste paste) {
        String resultMessage = storageService.uploadPaste(paste);
        return new ResponseEntity<>(resultMessage, HttpStatus.CREATED);
    }

    @GetMapping("/paste/{id}")
    public ResponseEntity<RequestPaste> getPaste(@PathVariable String id) {
        RequestPaste paste = storageService.getPasteFromStorage(id);
        return new ResponseEntity<>(paste,HttpStatus.OK);
    }

    @DeleteMapping("/deletePaste/{id}")
    public ResponseEntity<String> deletePaste(@PathVariable String id) {
        String resultMessage = storageService.deletePasteFromStorage(id);
        return new ResponseEntity<>(resultMessage,HttpStatus.NO_CONTENT);
    }

    @PostMapping("/editPaste")
    public ResponseEntity<String> editPaste(@RequestBody RequestPaste paste) {
        String resultMessage = storageService.updatePasteInStorage(paste);

        return new ResponseEntity<>(resultMessage,HttpStatus.NO_CONTENT);
    }


}
