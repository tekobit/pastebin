// for saving to cache when updating/creating

package com.zufarov.pastebinV1.pet.services;

import com.zufarov.pastebinV1.pet.dtos.PermissionDto;
import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.Permission;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.repositories.PastesRepository;
import com.zufarov.pastebinV1.pet.repositories.PermissionsRepository;
import com.zufarov.pastebinV1.pet.repositories.UsersRepository;
import com.zufarov.pastebinV1.pet.util.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final PastesRepository pastesRepository;
    private final UsersRepository usersRepository;
    private final PermissionsRepository permissionsRepository;

    @CachePut(value = "pasteContentCache",key="#pasteId")
    public String putPasteContentToCache(String content, String pasteId) {
        return content;
    }

    @CachePut(value = "pasteMetadataCache",key="#pasteId")
    public Paste putPasteMetadataToCache(Paste paste, String pasteId) {
        return paste;
    }


    @Cacheable(value = "permissionCache",key = "{#permissionDto.pasteId,#username}")
    public Permission findPermission(PermissionDto permissionDto, String username) {
        Optional<Paste> optionalPaste = pastesRepository.findById(permissionDto.pasteId());
        Optional<User> optionalUser = usersRepository.findByName(username);
        if (optionalUser.isEmpty() || optionalPaste.isEmpty()) {
            throw new NotFoundException("can't find user or paste");
        }
        Optional<Permission> optionalPermission = permissionsRepository.findByPasteAndUser(optionalPaste.get(),optionalUser.get());
        if (optionalPermission.isEmpty()) {
            throw new NotFoundException("can't find user or paste");
        }
        return optionalPermission.get();
    }

    @CachePut(value = "permissionCache",key="{#permission.paste.id,#permission.user.name}")
    public Permission putPermissionToCache(Permission permission) {
        return permission;
    }

}
