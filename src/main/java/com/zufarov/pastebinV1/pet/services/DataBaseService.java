//Used to save metadata to postgres

package com.zufarov.pastebinV1.pet.services;

import com.zufarov.pastebinV1.pet.components.AuthenticationFacade;
import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.Permission;
import com.zufarov.pastebinV1.pet.models.RequestModels.CreateRequestPaste;
import com.zufarov.pastebinV1.pet.models.RequestModels.RequestPaste;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.repositories.PastesRepository;
import com.zufarov.pastebinV1.pet.repositories.PermissionsRepository;
import com.zufarov.pastebinV1.pet.util.ForbiddenException;
import com.zufarov.pastebinV1.pet.util.NotFoundException;
import com.zufarov.pastebinV1.pet.util.PermissionType;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DataBaseService {
    private final PastesRepository pastesRepository;
    private final CustomUserDetailService customUserDetailService;
    private final PermissionsRepository permissionsRepository;
    private final AuthenticationFacade authenticationFacade;
    private final PermissionService permissionService;
    private final CacheService cacheService;

    public DataBaseService(PastesRepository pastesRepository, CustomUserDetailService customUserDetailService, PermissionsRepository permissionsRepository, AuthenticationFacade authenticationFacade, PermissionService permissionService, CacheService cacheService) {
        this.pastesRepository = pastesRepository;
        this.customUserDetailService = customUserDetailService;
        this.permissionsRepository = permissionsRepository;
        this.authenticationFacade = authenticationFacade;
        this.permissionService = permissionService;
        this.cacheService = cacheService;
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
        User currentUser = customUserDetailService.loadUserByUsername(currentPrincipalName).getUser();
        paste.setOwner(currentUser);

        paste.setContentLocation(Url);
        pastesRepository.save(paste);
        permissionService.addOwner(currentUser.getId(), pasteId);
        cacheService.putPasteMetadataToCache(paste, pasteId);

    }



    @Cacheable(value = "pasteMetadataCache")
    public Paste getPasteMetadata(String pasteId) {
        Optional<Paste> optionalPaste = pastesRepository.findById(pasteId);
        if (optionalPaste.isEmpty()) {
            throw new NotFoundException("there isn't paste with such id");
        }
        Paste paste = optionalPaste.get();
        checkIfUserHasRequiredPermission(paste,PermissionType.VIEWER);
        paste.setLastVisited(java.time.LocalDateTime.now());
        pastesRepository.save(paste);
        return  optionalPaste.get();
    }

    @CacheEvict(value = "pasteMetadataCache")
    @Transactional
    public void deletePasteMetadata(String pasteId) {
        Optional<Paste> optionalPaste = pastesRepository.findById(pasteId);
        if (optionalPaste.isEmpty()) {
            throw new NotFoundException("there isn't paste with such id");
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
            throw new NotFoundException("there isn't paste with such id");
        }
        Paste pasteToUpdate = optionalPaste.get();
        checkIfUserHasRequiredPermission(pasteToUpdate,PermissionType.EDITOR);
        pasteToUpdate.setExpiresAt(requestPaste.getExpiresAt());
        pasteToUpdate.setTitle(requestPaste.getTitle());
        pasteToUpdate.setVisibility(requestPaste.getVisibility());
        pastesRepository.save(pasteToUpdate);
        cacheService.putPasteMetadataToCache(pasteToUpdate, pasteToUpdate.getId());
    }

    private void checkIfUserHasRequiredPermission(Paste paste,PermissionType requiredPermission) {
        if (paste.getVisibility().equals("private") || requiredPermission.equals(PermissionType.EDITOR) || requiredPermission.equals(PermissionType.OWNER)) {
            List<Permission> permissions = paste.getPermissions();
            Authentication currentUser = authenticationFacade.getAuthentication();
            String currentUserName;
            if (currentUser == null) {
                currentUserName = "System";
            } else {
                currentUserName = currentUser.getName();
            }
            boolean userHasAccess = false;
            int permissionLevel = requiredPermission.ordinal();
            for (Permission permission : permissions) {
                if ((permission.getUser().getName().equals(currentUserName) || currentUserName.equals("System")) && PermissionType.valueOf(permission.getType()).ordinal() >= permissionLevel) {
                    userHasAccess = true;
                }
            }
            if (!userHasAccess) {
                throw new ForbiddenException("user has no access");
            }
        }
    }


}
