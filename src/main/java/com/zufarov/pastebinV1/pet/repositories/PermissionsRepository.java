package com.zufarov.pastebinV1.pet.repositories;

import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.Permission;
import com.zufarov.pastebinV1.pet.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface PermissionsRepository extends JpaRepository<Permission, Integer> {
    Optional<Permission> findById(int id);
    void deletePermissionByPasteId(String id);

    void deletePermissionById(int id);

    Optional<Permission> findByPasteAndUser(Paste paste, User user);

}