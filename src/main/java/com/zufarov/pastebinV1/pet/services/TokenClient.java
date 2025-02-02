//used to connect other microservice to get unique id

package com.zufarov.pastebinV1.pet.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "TokenRangeService", url = "http://localhost:7070")
public interface TokenClient {
    @GetMapping("/get-unique-id")
    public ResponseEntity<String> getUniqueId();
}
