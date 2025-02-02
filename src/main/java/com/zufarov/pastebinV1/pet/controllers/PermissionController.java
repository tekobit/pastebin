package com.zufarov.pastebinV1.pet.controllers;

import com.zufarov.pastebinV1.pet.models.RequestModels.RequestPermission;
import com.zufarov.pastebinV1.pet.services.PermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
// shit to be deleted only for testing
    @GetMapping("/getPermissionJSON")
    public RequestPermission getPermission() {
        return new RequestPermission();
    }

    @PostMapping("/addPermission")
    public ResponseEntity<String> addPermission(@RequestBody RequestPermission requestPermission) {
        String resultMessage = permissionService.savePermission(requestPermission);
        return new ResponseEntity<>(resultMessage, HttpStatus.CREATED);
    }
    @PostMapping("/editPermission")
    public ResponseEntity<String> editPermission(@RequestBody RequestPermission requestPermission) {
        String resultMessage = permissionService.editPermission(requestPermission);
        return new ResponseEntity<>(resultMessage, HttpStatus.NO_CONTENT);

    }
    @PostMapping("/deletePermission")
    public ResponseEntity<String> deletePermission(@RequestBody RequestPermission requestPermission) {
        String resultMessage = permissionService.deletePermission(requestPermission);
        return new ResponseEntity<>(resultMessage, HttpStatus.NO_CONTENT);

    }
}
