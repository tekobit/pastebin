package com.zufarov.pastebinV1.pet.controllers;

import com.zufarov.pastebinV1.pet.models.RequestModels.CreateRequestPaste;
import com.zufarov.pastebinV1.pet.models.RequestModels.RequestPaste;
import com.zufarov.pastebinV1.pet.services.PasteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class PasteController {
    private final PasteService pasteService;

    @Autowired
    public PasteController(PasteService pasteService) {
        this.pasteService = pasteService;
    }

    @PostMapping("/pastes")
    public ResponseEntity<String> createPaste(@RequestBody CreateRequestPaste paste) {
        String resultMessage = pasteService.uploadPaste(paste);
        return new ResponseEntity<>(resultMessage, HttpStatus.CREATED);
    }

    @GetMapping("/pastes/{id}")
    public ResponseEntity<RequestPaste> getPaste(@PathVariable String id) {
        RequestPaste paste = pasteService.getPaste(id);
        return new ResponseEntity<>(paste,HttpStatus.OK);
    }

    @DeleteMapping("/pastes/{id}")
    public ResponseEntity<String> deletePaste(@PathVariable String id) {
        String resultMessage = pasteService.deletePaste(id);
        return new ResponseEntity<>(resultMessage,HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/pastes/{id}")
    public ResponseEntity<String> editPaste(@PathVariable String id,@RequestBody RequestPaste paste) {
        if (!id.equals(paste.getId())) return ResponseEntity.badRequest().build();

        String resultMessage = pasteService.updatePaste(paste);
        return new ResponseEntity<>(resultMessage,HttpStatus.NO_CONTENT);
    }


}
