package com.zufarov.pastebinV1.pet.config;

import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.repositories.PastesRepository;
import com.zufarov.pastebinV1.pet.repositories.UsersRepository;
import com.zufarov.pastebinV1.pet.services.CustomUserDetailService;
import com.zufarov.pastebinV1.pet.services.PasteService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
@EnableScheduling
public class ExpiredDataCleanupConfig {

    private final UsersRepository usersRepository;
    private final CustomUserDetailService customUserDetailService;
    PastesRepository pastesRepository;
    PasteService pasteService;

    public ExpiredDataCleanupConfig(PastesRepository pastesRepository, PasteService pasteService, UsersRepository usersRepository, CustomUserDetailService customUserDetailService) {
        this.pastesRepository = pastesRepository;
        this.pasteService = pasteService;
        this.usersRepository = usersRepository;
        this.customUserDetailService = customUserDetailService;
    }


    @Scheduled(cron = "0 0 3 * * ?")
    public void deleteExpiredPastes() {
        List<Paste> allPastes =  pastesRepository.findAllByExpiresAtBefore(java.time.LocalDateTime.now()).get();
        for (Paste paste : allPastes) {
            pasteService.deletePaste(paste.getId());
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
