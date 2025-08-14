package com.zufarov.pastebinV1.pet.controllers;

import com.zufarov.pastebinV1.pet.models.RequestModels.RequestPermission;
import com.zufarov.pastebinV1.pet.services.PermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/permissions")
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    public ResponseEntity<String> addPermission(@RequestBody RequestPermission requestPermission) {
        String resultMessage = permissionService.savePermission(requestPermission);
        return new ResponseEntity<>(resultMessage, HttpStatus.CREATED);
    }

    @PatchMapping
    public ResponseEntity<String> editPermission(@RequestBody RequestPermission requestPermission) {
        String resultMessage = permissionService.editPermission(requestPermission);
        return new ResponseEntity<>(resultMessage, HttpStatus.NO_CONTENT);

    }

    @DeleteMapping
    public ResponseEntity<String> deletePermission(@RequestBody RequestPermission requestPermission) {
        String resultMessage = permissionService.deletePermission(requestPermission);
        return new ResponseEntity<>(resultMessage, HttpStatus.NO_CONTENT);

    }
}
