package com.zufarov.pastebinV1.pet.controllers;

import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.services.RegistrationService;
import com.zufarov.pastebinV1.pet.util.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserValidator userValidator;
    private final RegistrationService registrationService;

    @Autowired
    public AuthController(UserValidator userValidator, RegistrationService registrationService) {
        this.userValidator = userValidator;
        this.registrationService = registrationService;
    }


    @GetMapping("/registration")
    public User registrationPage() {
        return new User();
    }

    @PostMapping("/registration")
    public String performRegistration(@RequestBody User user) {
        registrationService.register(user);
        return "redirect:/auth/login";
    }
}
