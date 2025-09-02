//Used to save metadata to postgres

package com.zufarov.pastebinV1.pet.services;

import com.zufarov.pastebinV1.pet.components.AuthenticationFacade;
import com.zufarov.pastebinV1.pet.dtos.PasteRequestDto;
import com.zufarov.pastebinV1.pet.dtos.PasteUpdateDto;
import com.zufarov.pastebinV1.pet.mappers.PasteMapper;
import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.Permission;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.repositories.PastesRepository;
import com.zufarov.pastebinV1.pet.repositories.PermissionsRepository;
import com.zufarov.pastebinV1.pet.util.ForbiddenException;
import com.zufarov.pastebinV1.pet.util.NotFoundException;
import com.zufarov.pastebinV1.pet.util.PermissionType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DataBaseService {
    private final PastesRepository pastesRepository;
    private final CustomUserDetailService customUserDetailService;
    private final PermissionsRepository permissionsRepository;
    private final AuthenticationFacade authenticationFacade;
    private final PermissionService permissionService;
    private final CacheService cacheService;
    private final PasteMapper pasteMapper;

    @Transactional
    public void savePasteMetadata(PasteRequestDto createRequestPaste, String pasteId, String url) {
        Paste paste = pasteMapper.toPaste(createRequestPaste, pasteId);

        String currenUsername = authenticationFacade.getAuthentication().getName();
        User currentUser = customUserDetailService.loadUserByUsername(currenUsername).getUser();

        paste.setOwner(currentUser);
        paste.setContentLocation(url);

        pastesRepository.save(paste);
        permissionService.addOwner(currentUser, paste);
        cacheService.putPasteMetadataToCache(paste, pasteId);

    }

    //TODO add check if paste is expired
    @Transactional
    @Cacheable(value = "pasteMetadataCache")
    public Paste getPasteMetadata(String pasteId) {
        Paste paste = pastesRepository.findById(pasteId).orElseThrow(() -> new NotFoundException("there isn't paste with such id"));

        checkIfUserHasRequiredPermission(paste,PermissionType.VIEWER);
        paste.setLastVisited(java.time.LocalDateTime.now());

        return paste;
    }

    @CacheEvict(value = "pasteMetadataCache")
    @Transactional
    public void deletePasteMetadata(String pasteId) {
        Paste pasteToDelete = pastesRepository.findById(pasteId).orElseThrow(() -> new NotFoundException("there isn't paste with such id"));

        checkIfUserHasRequiredPermission(pasteToDelete,PermissionType.OWNER);

        permissionsRepository.deletePermissionByPasteId(pasteId);
        pastesRepository.deleteById(pasteId);
    }

    @Transactional
    public void updatePasteMetadata(PasteUpdateDto pasteUpdateDto, String id) {
        Paste pasteToUpdate = pastesRepository.findById(id).orElseThrow(() -> new NotFoundException("there isn't paste with such id"));

        pasteMapper.updatePaste(pasteUpdateDto,pasteToUpdate);

        checkIfUserHasRequiredPermission(pasteToUpdate,PermissionType.EDITOR);
        pastesRepository.save(pasteToUpdate);
        cacheService.putPasteMetadataToCache(pasteToUpdate, pasteToUpdate.getId());
    }

    private void checkIfUserHasRequiredPermission(Paste paste,PermissionType requiredPermission) {
        if (paste.getVisibility().equals("private") || requiredPermission.equals(PermissionType.EDITOR) || requiredPermission.equals(PermissionType.OWNER)) {
            List<Permission> permissions = paste.getPermissions();
            Authentication currentUser = authenticationFacade.getAuthentication();

            if (currentUser == null) {
                throw new ForbiddenException("user has no access");
            }

            String currentUserName = currentUser.getName();
            if (currentUserName.equals("System")) return;

            boolean userHasAccess = false;
            int permissionLevel = requiredPermission.ordinal();
            for (Permission permission : permissions) {
                if (permission.getUser().getName().equals(currentUserName) && permission.getType().ordinal() >= permissionLevel) {
                    userHasAccess = true;
                    break;
                }
            }
            if (!userHasAccess) {
                throw new ForbiddenException("user has no access");
            }
        }
    }


}
