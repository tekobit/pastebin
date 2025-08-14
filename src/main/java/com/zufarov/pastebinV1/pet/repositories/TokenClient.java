//used to connect other microservice to get unique id

package com.zufarov.pastebinV1.pet.repositories;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "TokenRangeService", url = "${token-service.url}")
public interface TokenClient {
    @GetMapping("/get-unique-id")
    public ResponseEntity<String> getUniqueId();
}
