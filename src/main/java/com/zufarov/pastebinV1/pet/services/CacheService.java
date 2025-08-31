// for saving to cache when updating/creating

package com.zufarov.pastebinV1.pet.services;

import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CacheService {

    @CachePut(value = "pasteContentCache",key="#pasteId", unless = "#result == null")
    public String putPasteContentToCache(String content, String pasteId) {
        return content;
    }

    @CachePut(value = "pasteMetadataCache",key="#pasteId", unless = "#result == null")
    public Paste putPasteMetadataToCache(Paste paste, String pasteId) {
        return paste;
    }

    @CachePut(value = "permissionCache",key="#permission.paste.id+'_'+#permission.user.name", unless = "#result == null")
    public Permission putPermissionToCache(Permission permission) {
        return permission;
    }

}
