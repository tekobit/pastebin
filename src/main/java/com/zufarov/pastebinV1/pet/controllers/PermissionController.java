package com.zufarov.pastebinV1.pet.controllers;

import com.zufarov.pastebinV1.pet.dtos.PermissionDto;
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
    public ResponseEntity<String> addPermission(@RequestBody PermissionDto permissionDto) {
        String resultMessage = permissionService.savePermission(permissionDto);
        return new ResponseEntity<>(resultMessage, HttpStatus.CREATED);
    }

    @PatchMapping
    public ResponseEntity<String> editPermission(@RequestBody PermissionDto permissionDto) {
        String resultMessage = permissionService.editPermission(permissionDto);
        return new ResponseEntity<>(resultMessage, HttpStatus.NO_CONTENT);

    }

    @DeleteMapping
    public ResponseEntity<String> deletePermission(@RequestBody PermissionDto permissionDto) {
        String resultMessage = permissionService.deletePermission(permissionDto);
        return new ResponseEntity<>(resultMessage, HttpStatus.NO_CONTENT);

    }
}
