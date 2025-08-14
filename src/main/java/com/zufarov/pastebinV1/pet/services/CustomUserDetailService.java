// used for login and registration maybe((

package com.zufarov.pastebinV1.pet.services;

import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.repositories.UsersRepository;
import com.zufarov.pastebinV1.pet.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailService implements UserDetailsService {
    private final UsersRepository usersRepository;

    @Autowired
    public CustomUserDetailService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

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
    public void deleteUser(String username) {
        usersRepository.deleteUserByName(username);
    }
}
