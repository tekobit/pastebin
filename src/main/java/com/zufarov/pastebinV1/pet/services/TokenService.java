//just realization of token client

package com.zufarov.pastebinV1.pet.services;

import com.zufarov.pastebinV1.pet.repositories.TokenClient;
import org.springframework.stereotype.Service;

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
