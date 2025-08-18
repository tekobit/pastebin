package com.zufarov.pastebinV1.pet.services;

import com.zufarov.pastebinV1.pet.components.AuthenticationFacade;
import com.zufarov.pastebinV1.pet.dtos.PermissionDto;
import com.zufarov.pastebinV1.pet.mappers.PermissionMapperService;
import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.Permission;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.repositories.PastesRepository;
import com.zufarov.pastebinV1.pet.repositories.PermissionsRepository;
import com.zufarov.pastebinV1.pet.repositories.UsersRepository;
import com.zufarov.pastebinV1.pet.util.ForbiddenException;
import com.zufarov.pastebinV1.pet.util.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

//TODO add checks e.g. if user the only owner, he cant delete his permission or make him editor/viewer

@RequiredArgsConstructor
@Service
public class PermissionService {
    private final PermissionsRepository permissionsRepository;
    private final UsersRepository usersRepository;
    private final PastesRepository pastesRepository;
    private final AuthenticationFacade authenticationFacade;
    private final CacheService cacheService;
    private final PermissionMapperService permissionMapperService;


    @Transactional
    public String savePermission(PermissionDto permissionDto) {
        checkIfCurrentUserOwner(cacheService.findPermission(permissionDto,authenticationFacade.getAuthentication().getName()));
        checkIfPermissionAlreadyExists(permissionDto);

        Permission permission = permissionMapperService.toPermission(permissionDto);
        permissionsRepository.save(permission);
        return "permission has been successfully saved";
    }

    @Transactional
    public String editPermission(PermissionDto permissionDto) {
        checkIfCurrentUserOwner(cacheService.findPermission(permissionDto,authenticationFacade.getAuthentication().getName()));

        Permission permission = cacheService.findPermission(permissionDto, permissionDto.username());
        permission.setType(permissionDto.type());

        permissionsRepository.save(permission);
        cacheService.putPermissionToCache(permission);
        return "permission has been updated successfully";
    }

    @Transactional
    @CacheEvict(value = "permissionCache",key = "{#permissionDto.pasteId,#permissionDto.username}")
    public String deletePermission(PermissionDto permissionDto) {
        checkIfCurrentUserOwner(cacheService.findPermission(permissionDto,authenticationFacade.getAuthentication().getName()));

        Permission permission = cacheService.findPermission(permissionDto, permissionDto.username());
        permissionsRepository.deletePermissionById(permission.getId());
        return "permission has been successfully deleted";
    }

    public void addOwner(User user, Paste paste) {
        Permission permission = permissionMapperService.createOwnerPermission(user, paste);

        permissionsRepository.save(permission);
        cacheService.putPermissionToCache(permission);
    }

    private void checkIfCurrentUserOwner(Permission currentUserPermission) {
        String currentUserPermissionType = currentUserPermission.getType();
        if (!currentUserPermissionType.equals("OWNER")) {
            throw new ForbiddenException("User has no access to this feature");
        }
    }

    private void checkIfPermissionAlreadyExists(PermissionDto permissionDto) {
        Optional<Paste> optionalPaste = pastesRepository.findById(permissionDto.pasteId());
        Optional<User> optionalUser = usersRepository.findByName(permissionDto.username());
        if (optionalUser.isEmpty() || optionalPaste.isEmpty()) {
            throw new NotFoundException("can't find permission");
        }
        Optional<Permission> optionalPermission = permissionsRepository.findByPasteAndUser(optionalPaste.get(),optionalUser.get());
        if (optionalPermission.isPresent()) {
            throw new ForbiddenException("such permission already exists");
        }
    }

}

