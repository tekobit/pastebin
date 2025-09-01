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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PermissionFinder {
    private final PermissionsRepository permissionsRepository;
    private final UsersRepository usersRepository;
    private final PastesRepository pastesRepository;

    @Cacheable(value = "permissionCache",key = "#permissionDto.pasteId+'_'+#username")
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
}

