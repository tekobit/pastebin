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
import com.zufarov.pastebinV1.pet.util.BadRequestException;
import com.zufarov.pastebinV1.pet.util.ForbiddenException;
import com.zufarov.pastebinV1.pet.util.NotFoundException;
import com.zufarov.pastebinV1.pet.util.PermissionType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@RequiredArgsConstructor
@Service
public class PermissionService {
    private final PermissionsRepository permissionsRepository;
    private final UsersRepository usersRepository;
    private final PastesRepository pastesRepository;
    private final AuthenticationFacade authenticationFacade;
    private final CacheService cacheService;
    private final PermissionMapperService permissionMapperService;
    private final PermissionFinder permissionFinder;


    @Transactional
    public String savePermission(PermissionDto permissionDto) {
        checkIfCurrentUserOwner(permissionFinder.findPermission(permissionDto,authenticationFacade.getAuthentication().getName()));
        checkIfPermissionAlreadyExists(permissionDto);

        Permission permission = permissionMapperService.toPermission(permissionDto);
        permissionsRepository.save(permission);
        return "permission has been successfully saved";
    }

    @Transactional
    public String editPermission(PermissionDto permissionDto) {
        checkIfCurrentUserOwner(permissionFinder.findPermission(permissionDto,authenticationFacade.getAuthentication().getName()));
        if (permissionDto.username().equals(authenticationFacade.getAuthentication().getName()) && isUserOnlyOwner(permissionDto)) {
            throw  new BadRequestException("You are not allowed to edit this permission");
        }

        Permission permission = permissionFinder.findPermission(permissionDto, permissionDto.username());
        permission.setType(permissionDto.type());

        permissionsRepository.save(permission);
        cacheService.putPermissionToCache(permission);
        return "permission has been updated successfully";
    }

    @Transactional
    @CacheEvict(value = "permissionCache",key = "#permissionDto.pasteId+'_'+#permissionDto.username")
    public String deletePermission(PermissionDto permissionDto) {
        checkIfCurrentUserOwner(permissionFinder.findPermission(permissionDto,authenticationFacade.getAuthentication().getName()));
        if (permissionDto.username().equals(authenticationFacade.getAuthentication().getName()) && isUserOnlyOwner(permissionDto)) {
            throw  new ForbiddenException("You are not allowed to delete this permission");
        }
        Permission permission = permissionFinder.findPermission(permissionDto, permissionDto.username());
        permissionsRepository.deletePermissionById(permission.getId());
        return "permission has been successfully deleted";
    }

    //need this only to create first owner permission for new paste
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

    private boolean isUserOnlyOwner(PermissionDto permissionDto) {
        return permissionsRepository.findAllByPaste_IdAndType(permissionDto.pasteId(), PermissionType.OWNER.name()).size() == 1;
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

