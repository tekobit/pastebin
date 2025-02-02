package com.zufarov.pastebinV1.pet.repositories;

import com.zufarov.pastebinV1.pet.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, Integer> {
    Optional<User> findByName(String name);
    Optional<User> findById(int id);
}
