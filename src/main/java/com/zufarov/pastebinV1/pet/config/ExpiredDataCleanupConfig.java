package com.zufarov.pastebinV1.pet.config;

import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.repositories.PastesRepository;
import com.zufarov.pastebinV1.pet.repositories.UsersRepository;
import com.zufarov.pastebinV1.pet.services.CustomUserDetailService;
import com.zufarov.pastebinV1.pet.services.PasteService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ExpiredDataCleanupConfig {

    private final UsersRepository usersRepository;
    private final CustomUserDetailService customUserDetailService;
    private final PastesRepository pastesRepository;
    private final PasteService pasteService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void deleteExpiredPastes() {

        try {
            Authentication systemAuthentication = new UsernamePasswordAuthenticationToken(
                    "System",null, Collections.singleton(new SimpleGrantedAuthority("SYSTEM"))
            );
            SecurityContextHolder.getContext().setAuthentication(systemAuthentication);
            List<Paste> allPastes =  pastesRepository.findAllByExpiresAtBefore(java.time.LocalDateTime.now()).get();
            for (Paste paste : allPastes) {
                pasteService.deletePaste(paste.getId());
            }
        } finally {
            SecurityContextHolder.clearContext();
        }

    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void deleteNonActiveUsers() {
        List<User> allUsers =  usersRepository.findAllByLastLoginBefore(java.time.LocalDateTime.now().minusMonths(6)).get();
        for (User user : allUsers) {
            customUserDetailService.deleteUser(user.getName());
        }
    }
}
