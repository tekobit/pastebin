package com.zufarov.pastebinV1.pet.util;

import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.services.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component

public class PasteValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Paste paste = (Paste) target;

    }

}
