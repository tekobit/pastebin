//Used to save metadata to postgres

package com.zufarov.pastebinV1.pet.services;

import com.zufarov.pastebinV1.pet.components.AuthenticationFacade;
import com.zufarov.pastebinV1.pet.models.RequestModels.CreateRequestPaste;
import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.Permission;
import com.zufarov.pastebinV1.pet.models.RequestModels.RequestPaste;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.repositories.PastesRepository;
import com.zufarov.pastebinV1.pet.repositories.PermissionsRepository;
import com.zufarov.pastebinV1.pet.util.BadRequestException;
import com.zufarov.pastebinV1.pet.util.ForbiddenException;
import com.zufarov.pastebinV1.pet.util.PermissionType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DataBaseService {
    private final PastesRepository pastesRepository;
    private final UserDetailService userDetailService;
    private final PermissionsRepository permissionsRepository;
    private final AuthenticationFacade authenticationFacade;

    public DataBaseService(PastesRepository pastesRepository, UserDetailService userDetailService, PermissionsRepository permissionsRepository, AuthenticationFacade authenticationFacade) {
        this.pastesRepository = pastesRepository;
        this.userDetailService = userDetailService;
        this.permissionsRepository = permissionsRepository;
        this.authenticationFacade = authenticationFacade;
    }

    @Transactional
    public void savePasteMetadata(CreateRequestPaste createRequestPaste, String pasteId, String Url) {
        Paste paste = new Paste();
        paste.setId(pasteId);
        paste.setTitle(createRequestPaste.getTitle());
        paste.setVisibility(createRequestPaste.getVisibility());
        paste.setCreatedAt(java.time.LocalDateTime.now());
        paste.setLastVisited(java.time.LocalDateTime.now());
        paste.setExpiresAt(java.time.LocalDateTime.now());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        User currentUser = userDetailService.loadUserByUsername(currentPrincipalName).getUser();
        paste.setOwner(currentUser);

        paste.setContentLocation(Url);
        pastesRepository.save(paste);
        savePermissions(currentUser.getId(), pasteId);


    }

    private void savePermissions(int userId,String pasteId) {
        Permission permission = new Permission(new User(),new Paste());
        permission.getUser().setId(userId);
        permission.getPaste().setId(pasteId);
        permission.setCreatedAt(java.time.LocalDateTime.now());
        permission.setType("OWNER");
        permissionsRepository.save(permission);
    }

    public Paste getPasteMetadata(String pasteId) {
        Optional<Paste> optionalPaste = pastesRepository.findById(pasteId);
        if (optionalPaste.isEmpty()) {
            throw new BadRequestException("there isn't paste with such id");
        }
        Paste paste = optionalPaste.get();
        checkIfUserHasRequiredPermission(paste,PermissionType.VIEWER);
        paste.setLastVisited(java.time.LocalDateTime.now());
        pastesRepository.save(paste);
        return  optionalPaste.get();
    }

    @Transactional
    public void deletePasteMetadata(String pasteId) {
        Optional<Paste> optionalPaste = pastesRepository.findById(pasteId);
        if (optionalPaste.isEmpty()) {
            throw new BadRequestException("there isn't paste with such id");
        }
        Paste pasteToDelete = optionalPaste.get();
        checkIfUserHasRequiredPermission(pasteToDelete,PermissionType.OWNER);
        permissionsRepository.deletePermissionByPasteId(pasteId);
        pastesRepository.deleteById(pasteId);
    }

    @Transactional
    public void updatePasteMetadata(RequestPaste requestPaste) {
        Optional<Paste> optionalPaste = pastesRepository.findById(requestPaste.getId());
        if (optionalPaste.isEmpty()) {
            throw new BadRequestException("there isn't paste with such id");
        }
        Paste pasteToUpdate = optionalPaste.get();
        checkIfUserHasRequiredPermission(pasteToUpdate,PermissionType.EDITOR);
        pasteToUpdate.setExpiresAt(requestPaste.getExpiresAt());
        pasteToUpdate.setTitle(requestPaste.getTitle());
        pasteToUpdate.setVisibility(requestPaste.getVisibility());
        pastesRepository.save(pasteToUpdate);

    }

    private void checkIfUserHasRequiredPermission(Paste paste,PermissionType requiredPermission) {
        if (paste.getVisibility().equals("private") || requiredPermission.equals(PermissionType.EDITOR) || requiredPermission.equals(PermissionType.OWNER)) {
            List<Permission> permissions = paste.getPermissions();
            String currentUserName = authenticationFacade.getAuthentication().getName();
            boolean userHasAccess = false;
            int permissionLevel = requiredPermission.ordinal();
            for (Permission permission : permissions) {
                if (permission.getUser().getName().equals(currentUserName) && PermissionType.valueOf(permission.getType()).ordinal() >= permissionLevel) {
                    userHasAccess = true;
                }
            }
            if (!userHasAccess) {
                throw new ForbiddenException("user has no access");
            }
        }
    }
}
