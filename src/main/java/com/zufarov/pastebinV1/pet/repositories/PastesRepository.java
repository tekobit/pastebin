package com.zufarov.pastebinV1.pet.repositories;

import com.zufarov.pastebinV1.pet.models.Paste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PastesRepository extends JpaRepository<Paste, String> {

    @NonNull
    Optional<Paste> findById(@NonNull String id);
    Optional<List<Paste>> findAllByExpiresAtBefore(java.time.LocalDateTime expiresAt);
}
