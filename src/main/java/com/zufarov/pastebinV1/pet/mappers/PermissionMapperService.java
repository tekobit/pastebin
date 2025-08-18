package com.zufarov.pastebinV1.pet.mappers;

import com.zufarov.pastebinV1.pet.dtos.PermissionDto;
import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.Permission;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.repositories.PastesRepository;
import com.zufarov.pastebinV1.pet.repositories.UsersRepository;
import com.zufarov.pastebinV1.pet.util.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PermissionMapperService {
    private final PastesRepository pastesRepository;
    private final UsersRepository usersRepository;

    public Permission toPermission(PermissionDto permissionDto) {
        Permission permission = new Permission();
        User user = usersRepository.findByName(permissionDto.username()).orElseThrow(() -> new NotFoundException("can't find user or paste"));
        Paste paste = pastesRepository.findById(permissionDto.pasteId()).orElseThrow(() -> new NotFoundException("can't find user or paste"));

        permission.setPaste(paste);
        permission.setUser(user);
        permission.setType(permissionDto.type());
        permission.setCreatedAt(java.time.LocalDateTime.now());
        return permission;
    }

    public Permission createOwnerPermission(User user, Paste paste) {
        Permission permission = new Permission(user,paste);
        permission.setCreatedAt(java.time.LocalDateTime.now());
        permission.setType("OWNER");

        return permission;
    }
}
