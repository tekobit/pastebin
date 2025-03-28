package com.zufarov.pastebinV1.pet.util;

import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.services.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {
    private final UserDetailService userDetailService;

    @Autowired
    public UserValidator(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;
        try {
            userDetailService.loadUserByUsername(user.getName());

        } catch (UsernameNotFoundException e) {
            return;
        }
        errors.rejectValue("name", "", "user with this username already exists");

    }

}
