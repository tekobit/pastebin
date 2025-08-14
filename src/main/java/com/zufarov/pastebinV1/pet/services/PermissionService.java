package com.zufarov.pastebinV1.pet.services;

import com.zufarov.pastebinV1.pet.components.AuthenticationFacade;
import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.Permission;
import com.zufarov.pastebinV1.pet.models.RequestModels.RequestPermission;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.repositories.PastesRepository;
import com.zufarov.pastebinV1.pet.repositories.PermissionsRepository;
import com.zufarov.pastebinV1.pet.repositories.UsersRepository;
import com.zufarov.pastebinV1.pet.util.ForbiddenException;
import com.zufarov.pastebinV1.pet.util.NotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
//scary code here for me
@Service
public class PermissionService {
    private final PermissionsRepository permissionsRepository;
    private final UsersRepository usersRepository;
    private final PastesRepository pastesRepository;
    private final AuthenticationFacade authenticationFacade;
    private final CacheService cacheService;

    public PermissionService(PermissionsRepository permissionsRepository, UsersRepository usersRepository,
                             PastesRepository pastesRepository, AuthenticationFacade authenticationFacade,  CacheService cacheService) {
        this.permissionsRepository = permissionsRepository;
        this.usersRepository = usersRepository;
        this.pastesRepository = pastesRepository;
        this.authenticationFacade = authenticationFacade;
        this.cacheService = cacheService;
    }

    @Transactional
    public String savePermission(RequestPermission requestPermission) {
        checkIfCurrentUserOwner(cacheService.findPermission(requestPermission,authenticationFacade.getAuthentication().getName()));
        checkIfPermissionAlreadyExists(requestPermission);
        Permission permission = new Permission();
        permission.setType(requestPermission.getType());
        Optional<User> optionalUser = usersRepository.findByName(requestPermission.getUsername());
        Optional<Paste> optionalPaste = pastesRepository.findById(requestPermission.getPasteId());
        if (optionalUser.isEmpty() || optionalPaste.isEmpty()) {
            throw new NotFoundException("can't find user or paste");
        }
        permission.setUser(optionalUser.get());
        permission.setPaste(optionalPaste.get());
        permission.setCreatedAt(java.time.LocalDateTime.now());
        permissionsRepository.save(permission);
        return "permission has been successfully saved";
    }

    @Transactional
    public String editPermission(RequestPermission requestPermission) {
        checkIfCurrentUserOwner(cacheService.findPermission(requestPermission,authenticationFacade.getAuthentication().getName()));

        Permission permission = cacheService.findPermission(requestPermission, requestPermission.getUsername());
        permission.setType(requestPermission.getType());
        permissionsRepository.save(permission);
        cacheService.putPermissionToCache(permission);
        return "permission has been updated successfully";
    }

    @Transactional
    @CacheEvict(value = "permissionCache",key = "{#requestPermission.pasteId,#requestPermission.username}")
    public String deletePermission(RequestPermission requestPermission) {
        checkIfCurrentUserOwner(cacheService.findPermission(requestPermission,authenticationFacade.getAuthentication().getName()));

        Permission permission = cacheService.findPermission(requestPermission, requestPermission.getUsername());
        permissionsRepository.deletePermissionById(permission.getId());
        return "permission has been successfully deleted";
    }



    public void addOwner(int userId, String pasteId) {
        Permission permission = new Permission(new User(),new Paste());
        permission.getUser().setId(userId);
        permission.getPaste().setId(pasteId);
        permission.setCreatedAt(java.time.LocalDateTime.now());
        permission.setType("OWNER");
        permissionsRepository.save(permission);
        cacheService.putPermissionToCache(permission);
    }

    private void checkIfCurrentUserOwner(Permission currentUserPermission) {
        String currentUserPermissionType = currentUserPermission.getType();
        if (!currentUserPermissionType.equals("OWNER")) {
            throw new ForbiddenException("User has no access to this feature");
        }
    }

    private void checkIfPermissionAlreadyExists(RequestPermission requestPermission) {
        Optional<Paste> optionalPaste = pastesRepository.findById(requestPermission.getPasteId());
        Optional<User> optionalUser = usersRepository.findByName(requestPermission.getUsername());
        if (optionalUser.isEmpty() || optionalPaste.isEmpty()) {
            throw new NotFoundException("can't find permission");
        }
        Optional<Permission> optionalPermission = permissionsRepository.findByPasteAndUser(optionalPaste.get(),optionalUser.get());
        if (optionalPermission.isPresent()) {
            throw new ForbiddenException("such permission already exists");
        }
    }

}

