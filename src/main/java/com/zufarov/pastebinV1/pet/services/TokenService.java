//just realization of token client

package com.zufarov.pastebinV1.pet.services;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenService {
    private static TokenClient tokenClient;

    public TokenService(TokenClient tokenClient) {
        TokenService.tokenClient = tokenClient;
    }
    public String getUniqueId() {
        return tokenClient.getUniqueId().getBody();
    }


}
