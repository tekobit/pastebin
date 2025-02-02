package com.zufarov.pastebinV1.pet.services;

import com.zufarov.pastebinV1.pet.components.AuthenticationFacade;
import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.Permission;
import com.zufarov.pastebinV1.pet.models.RequestModels.RequestPermission;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.repositories.PastesRepository;
import com.zufarov.pastebinV1.pet.repositories.PermissionsRepository;
import com.zufarov.pastebinV1.pet.repositories.UsersRepository;
import com.zufarov.pastebinV1.pet.util.BadRequestException;
import com.zufarov.pastebinV1.pet.util.ForbiddenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
public class PermissionService {
    private final PermissionsRepository permissionsRepository;
    private final UsersRepository usersRepository;
    private final PastesRepository pastesRepository;
    private final AuthenticationFacade authenticationFacade;

    public PermissionService(PermissionsRepository permissionsRepository, UsersRepository usersRepository, PastesRepository pastesRepository, AuthenticationFacade authenticationFacade) {
        this.permissionsRepository = permissionsRepository;
        this.usersRepository = usersRepository;
        this.pastesRepository = pastesRepository;
        this.authenticationFacade = authenticationFacade;
    }
    @Transactional
    public String savePermission(RequestPermission requestPermission) {
        checkIfCurrentUserOwner(findPermission(requestPermission,authenticationFacade.getAuthentication().getName()));
        checkIfPermissionAlreadyExists(requestPermission);
        Permission permission = new Permission();
        permission.setType(requestPermission.getType());
        Optional<User> optionalUser = usersRepository.findByName(requestPermission.getUsername());
        Optional<Paste> optionalPaste = pastesRepository.findById(requestPermission.getPasteId());
        if (optionalUser.isEmpty() || optionalPaste.isEmpty()) {
            throw new BadRequestException("can't find user or paste");
        }
        permission.setUser(optionalUser.get());
        permission.setPaste(optionalPaste.get());
        permission.setCreatedAt(java.time.LocalDateTime.now());
        permissionsRepository.save(permission);
        return "permission has been successfully saved";
    }
    @Transactional
    public String editPermission(RequestPermission requestPermission) {
        checkIfCurrentUserOwner(findPermission(requestPermission,authenticationFacade.getAuthentication().getName()));

        Permission permission = findPermission(requestPermission, requestPermission.getUsername());
        permission.setType(requestPermission.getType());
        permissionsRepository.save(permission);
        return "permission has been updated successfully";
    }
    @Transactional
    public String deletePermission(RequestPermission requestPermission) {
        checkIfCurrentUserOwner(findPermission(requestPermission,authenticationFacade.getAuthentication().getName()));

        Permission permission = findPermission(requestPermission, requestPermission.getUsername());
        permissionsRepository.deletePermissionById(permission.getId());
        return "permission has been successfully deleted";
    }
    private Permission findPermission(RequestPermission requestPermission,String username) {
        Optional<Paste> optionalPaste = pastesRepository.findById(requestPermission.getPasteId());
        Optional<User> optionalUser = usersRepository.findByName(username);
        if (optionalUser.isEmpty() || optionalPaste.isEmpty()) {
            throw new BadRequestException("can't find permission");
        }
        Optional<Permission> optionalPermission = permissionsRepository.findByPasteAndUser(optionalPaste.get(),optionalUser.get());
        if (optionalPermission.isEmpty()) {
            throw new BadRequestException("can't find permission");
        }
        return optionalPermission.get();
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
            throw new BadRequestException("can't find permission");
        }
        Optional<Permission> optionalPermission = permissionsRepository.findByPasteAndUser(optionalPaste.get(),optionalUser.get());
        if (optionalPermission.isPresent()) {
            throw new BadRequestException("such permission already exists");
        }
    }
}
