//Used to register user

package com.zufarov.pastebinV1.pet.services;

import com.zufarov.pastebinV1.pet.dtos.UserRequestDto;
import com.zufarov.pastebinV1.pet.mappers.UserMapper;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public void register(UserRequestDto userRequestDto) {
        User user = userMapper.toUser(userRequestDto,passwordEncoder);
        usersRepository.save(user);
    }
}
