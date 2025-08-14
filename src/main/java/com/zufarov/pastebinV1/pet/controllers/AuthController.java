package com.zufarov.pastebinV1.pet.controllers;

import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.services.RegistrationService;
import com.zufarov.pastebinV1.pet.util.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserValidator userValidator;
    private final RegistrationService registrationService;
    private final JwtEncoder encoder;

    @Autowired
    public AuthController(UserValidator userValidator, RegistrationService registrationService, JwtEncoder encoder) {
        this.userValidator = userValidator;
        this.registrationService = registrationService;
        this.encoder = encoder;
    }

    @PostMapping("/registration")
    public ResponseEntity<Void> registration(@RequestBody User user) {
        registrationService.register(user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public String login(Authentication authentication) {
        Instant now = Instant.now();

        // lifetime of jwt token
        long expires = 36000L;

        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining( " "));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expires))
                .subject(authentication.getName())
                .claim("scope",scope)
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
