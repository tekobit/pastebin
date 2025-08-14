package com.zufarov.pastebinV1.pet.repositories;

import org.springframework.security.core.Authentication;

public interface IAuthenticationFacade {
    Authentication getAuthentication();
}
