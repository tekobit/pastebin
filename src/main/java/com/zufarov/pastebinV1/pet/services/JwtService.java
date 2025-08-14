package com.zufarov.pastebinV1.pet.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Map;

@Service
public class JwtService {
    @Value("${token.signing.key}")
    private String jwtSigningKey;

    // generating token



    // for getting key to sign jwt
    private Key getSigningKey(String token) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
