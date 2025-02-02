package com.zufarov.pastebinV1.pet.controllers;

import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.repositories.PastesRepository;
import com.zufarov.pastebinV1.pet.services.AdminService;
import com.zufarov.pastebinV1.pet.services.StorageService;
import com.zufarov.pastebinV1.pet.services.TokenService;
import com.zufarov.pastebinV1.pet.util.PasteValidator;
import com.zufarov.pastebinV1.pet.util.TextValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MainController {
    private final PastesRepository pastesRepository;
    private final TokenService tokenService;
    private final AdminService adminService;
    private final PasteValidator pasteValidator;
    private final StorageService storageService;
    private final TextValidator textValidator;

    @Autowired
    public MainController(PastesRepository pastesRepository, TokenService tokenService, AdminService adminService, StorageService storageService, PasteValidator pasteValidator, TextValidator textValidator) {
        this.pastesRepository = pastesRepository;
        this.tokenService = tokenService;
        this.adminService = adminService;
        this.pasteValidator = pasteValidator;
        this.textValidator = textValidator;
        this.storageService = storageService;
    }

    @GetMapping("/profile")
    public String sayXyi() {
        return "profile";
    }

    @PostMapping("/profile")
    public String sayXy() {
        System.out.println("post request!!!!!!!!");
        return "post";
    }

    @GetMapping("/admin")
    public String adminPage() {
        adminService.doAdminStuff();

        return "admin";
    }
}

