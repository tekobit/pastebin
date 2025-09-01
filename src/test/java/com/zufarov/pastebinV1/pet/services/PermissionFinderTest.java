package com.zufarov.pastebinV1.pet.services;

import com.redis.testcontainers.RedisContainer;
import com.zufarov.pastebinV1.TestDataFactory;
import com.zufarov.pastebinV1.pet.dtos.PermissionDto;
import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.Permission;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.repositories.PastesRepository;
import com.zufarov.pastebinV1.pet.repositories.PermissionsRepository;
import com.zufarov.pastebinV1.pet.repositories.UsersRepository;
import com.zufarov.pastebinV1.pet.util.NotFoundException;
import com.zufarov.pastebinV1.pet.util.PermissionType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Testcontainers
@SpringBootTest
class PermissionFinderTest {

    @Autowired
    PermissionFinder permissionFinder;

    @SpyBean
    UsersRepository usersRepository;

    @SpyBean
    PastesRepository pastesRepository;

    @SpyBean
    PermissionsRepository permissionsRepository;

    @Autowired
    CacheManager cacheManager;

    // postgres
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",postgres::getJdbcUrl);
        registry.add("spring.datasource.username",postgres::getUsername);
        registry.add("spring.datasource.password",postgres::getPassword);
        registry.add("spring.jpa.generate-ddl", () -> true);
    }
    // redis

    @Container
    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:latest")).withExposedPorts(6379);
    ;
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", redisContainer::getFirstMappedPort);
    }

    @BeforeEach
    void setUp() {
        permissionsRepository.deleteAll();
        pastesRepository.deleteAll();
        usersRepository.deleteAll();
        cacheManager.getCacheNames().forEach(e -> cacheManager.getCache(e).clear());
    }

    private final String userName = "name";
    private final String email = "email";

    @Nested
    class FindPermissionSuccessfulTests {
        User user;
        Paste paste;
        Permission permission;
        PermissionDto permissionDto;

        @BeforeEach
        void setUp() {
            user = createAndSaveUser(userName,email);
            paste = createAndSavePaste(user);
            permission = createAndSavePermission(user,paste, PermissionType.OWNER);

            permissionDto = TestDataFactory.createPermissionDto(user,paste,permission.getType());
        }


        @Test
        void findPermission_ShouldReturnPermission_WhenPermissionExists() {
            Permission receivedPermission = permissionFinder.findPermission(permissionDto,user.getName());

            assertEquals(permission,receivedPermission);
        }

        @Test
        void findPermission_ShouldUseCacheOnSecondCall_WhenPermissionExists() {
            permissionFinder.findPermission(permissionDto,user.getName());
            permissionFinder.findPermission(permissionDto,user.getName());

            verify(pastesRepository,times(1)).findById(permissionDto.pasteId());
            verify(usersRepository,times(1)).findByName(user.getName());
            verify(permissionsRepository,times(1)).findByPasteAndUser(paste,user);
        }
    }

    @Nested
    class FindPermissionFailedTests {

        private final String NON_EXISTENT_USER_NAME = "non_existent_user";
        private final String NON_EXISTENT_PASTE_ID = "non_existent_paste_id";
        User user;
        Paste paste;

        @BeforeEach
        void setUp() {
            user = createAndSaveUser(userName,email);
            paste = createAndSavePaste(user);
        }

        @Test
        void findPermission_ShouldReturnNotFoundException_WhenUserDoesNotExist() {
            Permission permission = createAndSavePermission(user,paste, PermissionType.OWNER);

            PermissionDto permissionDto = new PermissionDto(permission.getType(),paste.getId(),null);

            assertThrows(NotFoundException.class,() -> permissionFinder.findPermission(permissionDto,NON_EXISTENT_USER_NAME));
        }
        @Test
        void findPermission_ShouldReturnNotFoundException_WhenPasteDoesNotExist() {
            PermissionDto permissionDto = new PermissionDto(PermissionType.OWNER.name(),NON_EXISTENT_PASTE_ID,null);

            assertThrows(NotFoundException.class,() -> permissionFinder.findPermission(permissionDto,userName));
        }

        @Test
        void findPermission_ShouldReturnNotFoundException_WhenPermissionDoesNotExist() {
            PermissionDto permissionDto = new PermissionDto(PermissionType.OWNER.name(),paste.getId(),null);

            assertThrows(NotFoundException.class,() -> permissionFinder.findPermission(permissionDto,userName));
        }
    }

    private @NotNull User createAndSaveUser(String name, String email) {
        User user = new User(name,email,"password", LocalDateTime.now(), LocalDateTime.now(),"ROLE_USER");
        usersRepository.save(user);
        return user;
    }

    private @NotNull Paste createAndSavePaste(User user) {
        Paste paste = new Paste();
        paste.setId("paste_id");
        paste.setTitle("paste_title");
        paste.setContentLocation("location");
        paste.setCreatedAt(LocalDateTime.now());
        paste.setExpiresAt(LocalDateTime.now().plusDays(15));
        paste.setVisibility("public");
        paste.setLastVisited(LocalDateTime.now());
        paste.setOwner(user);
        pastesRepository.save(paste);
        return paste;
    }


    private Permission createAndSavePermission(User user, Paste paste, PermissionType permissionType) {
        Permission permission = new Permission(user,paste);
        permission.setType(permissionType.name());
        permissionsRepository.save(permission);
        return permission;
    }
}