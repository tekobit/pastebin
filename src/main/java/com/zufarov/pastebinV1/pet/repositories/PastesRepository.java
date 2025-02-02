package com.zufarov.pastebinV1.pet.repositories;

import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.Permission;
import com.zufarov.pastebinV1.pet.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PastesRepository extends JpaRepository<Paste, String> {
    Optional<Paste> findById(String id);
}
