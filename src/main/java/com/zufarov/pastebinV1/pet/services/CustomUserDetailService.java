// used for login and registration maybe((

package com.zufarov.pastebinV1.pet.services;

import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.repositories.UsersRepository;
import com.zufarov.pastebinV1.pet.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UsersRepository usersRepository;

    @Override
    @Cacheable(value = "userCache")
    public CustomUserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        Optional<User> user = usersRepository.findByName(name);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return new CustomUserDetails(user.get());
    }
    @CacheEvict(value = "userCache")
    @Transactional
    public void deleteUser(String username) {
        usersRepository.deleteUserByName(username);
    }
}
