package com.zufarov.pastebinV1.pet.services;

import com.redis.testcontainers.RedisContainer;
import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.Permission;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.util.PermissionType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class CacheServiceTest {
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    CacheService cacheService;

    @Container
    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:latest")).withExposedPorts(6379);
    ;
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", redisContainer::getFirstMappedPort);
    }

    private final String pasteId = "id";

    // need counters to create correctly entities permission and users without repository, because of GenerationType.IDENTITY
    private int userCounter;
    private int permissionCounter;

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(e -> cacheManager.getCache(e).clear());
        userCounter = 1;
        permissionCounter = 1;
    }

    @Nested
    class PutPasteContentToCacheTests {
        @Test
        void putPasteContentToCache_newContent_mustAdd() {
            String pasteContent = "pasteContent";

            cacheService.putPasteContentToCache(pasteContent,pasteId);
            String contentFromCache = cacheManager.getCache("pasteContentCache").get(pasteId,String.class);


            assertThat(contentFromCache).isEqualTo(pasteContent);
        }


        @Test
        void putPasteContentToCache_existingContentInCache_mustUpdate() {
            String pasteContentOld = "pasteContentOld";
            String pasteContentNew = "pasteContentNew";

            cacheService.putPasteContentToCache(pasteContentOld,pasteId);
            cacheService.putPasteContentToCache(pasteContentNew,pasteId);

            String contentFromCache = cacheManager.getCache("pasteContentCache").get(pasteId,String.class);

            assertThat(contentFromCache).isEqualTo(pasteContentNew);

        }
        @Test
        void putPasteContentToCache_nullContent_mustThrowException() {
            String pasteContent = null;

            cacheService.putPasteContentToCache(pasteContent,pasteId);

            assertThrows(NullPointerException.class, () ->  cacheManager.getCache("pasteContentCache").get(pasteId).get());
        }
    }

    @Nested
    class PutPasteMetadataToCache{
        @Test
        void putPasteMetadataToCache_newMetadata_mustAdd() {
            User user = createUser("name", "email");
            Paste paste = createPaste(user);

            cacheService.putPasteMetadataToCache(paste,pasteId);
            Paste pasteFromCache = cacheManager.getCache("pasteMetadataCache").get(pasteId,Paste.class);

            assertThat(pasteFromCache).isEqualTo(paste);
        }
        @Test
        void putPasteMetadataToCache_existingMetadata_mustUpdate() {
            User oldUser = createUser("name", "email");
            Paste oldPaste = createPaste(oldUser);
            cacheService.putPasteMetadataToCache(oldPaste,pasteId);

            User newUser = createUser("new_name", "new_email");
            Paste newPaste = createNewPaste(newUser);
            cacheService.putPasteMetadataToCache(newPaste,pasteId);


            Paste pasteFromCache = cacheManager.getCache("pasteMetadataCache").get(pasteId,Paste.class);

            assertThat(pasteFromCache).isEqualTo(newPaste);
        }

        @Test
        void putPasteMetadataToCache_nullMetadata_mustThrowException() {
            Paste paste = null;

            cacheService.putPasteMetadataToCache(paste,pasteId);
            assertThrows(NullPointerException.class,() -> cacheManager.getCache("pasteMetadataCache").get(pasteId).get());

        }
    }

    @Nested
    class PutPermissionToCacheTests {
        @Test
        void putPermissionToCache_newPermission_mustAdd() {
            User user = createUser("name", "email");
            Paste paste = createPaste(user);

            Permission permission = createPermission(user,paste,PermissionType.OWNER);

            cacheService.putPermissionToCache(permission);

            Permission permissionCache = cacheManager.getCache("permissionCache").get(permission.getPaste().getId()+'_'+permission.getUser().getName(),Permission.class);

            assertThat(permissionCache).isEqualTo(permission);
        }
        @Test
        void putPermissionToCache_existingPermission_mustUpdate() {
            User user = createUser("name", "email");
            Paste oldPaste = createPaste(user);
            Permission oldPermission = createPermission(user,oldPaste,PermissionType.OWNER);

            cacheService.putPermissionToCache(oldPermission);

            Paste newPaste = createNewPaste(user);
            newPaste.setId(oldPaste.getId());
            Permission newPermission = createPermission(user,newPaste,PermissionType.EDITOR);

            cacheService.putPermissionToCache(newPermission);

            Permission permissionCache = cacheManager.getCache("permissionCache").get(oldPermission.getPaste().getId()+'_'+oldPermission.getUser().getName(),Permission.class);

            assertThat(permissionCache).isEqualTo(newPermission);
            assertThat(permissionCache.getPaste()).isEqualTo(newPaste);
        }

        @Test
        void putPermissionToCache_newPermissionNullPaste_mustThrowException() {
            User user = createUser("name", "email");
            Paste paste = null;

            Permission permission = createPermission(user,paste,PermissionType.OWNER);

            assertThrows(SpelEvaluationException.class,() -> cacheService.putPermissionToCache(permission));
        }
        @Test
        void putPermissionToCache_newPermissionNullUser_mustThrowException() {
            User user  = null;
            Paste paste = createPaste(user);

            Permission permission = createPermission(user,paste,PermissionType.OWNER);

            assertThrows(SpelEvaluationException.class,() -> cacheService.putPermissionToCache(permission));
        }
        @Test
        void putPermissionToCache_nullPermission_mustThrowException() {
            Permission permission = null;
            cacheService.putPermissionToCache(permission);
            assertThrows(NullPointerException.class,() -> cacheManager.getCache("permissionCache").get(permission.getId()+'_'+permission.getUser().getName()).get());
        }

    }

    private @NotNull User createUser(String name, String email) {
        User user = new User(name,email,"password", LocalDateTime.now(), LocalDateTime.now(),"ROLE_USER");
        user.setId(userCounter++);
        return user;
    }

    private @NotNull Paste createPaste(User user) {
        Paste paste = new Paste();
        paste.setId("public-id");
        paste.setTitle("public-title");
        paste.setContentLocation("location");
        paste.setCreatedAt(LocalDateTime.now());
        paste.setExpiresAt(LocalDateTime.now().plusDays(15));
        paste.setVisibility("public");
        paste.setLastVisited(LocalDateTime.now());
        paste.setOwner(user);
        return paste;
    }
    private @NotNull Paste createNewPaste(User user) {
        Paste paste = new Paste();
        paste.setId("new_public-id");
        paste.setTitle("new_public-title");
        paste.setContentLocation("new_location");
        paste.setCreatedAt(LocalDateTime.now().plusDays(10));
        paste.setExpiresAt(LocalDateTime.now().plusDays(25));
        paste.setVisibility("private");
        paste.setLastVisited(LocalDateTime.now().plusDays(10));
        paste.setOwner(user);
        return paste;
    }

    private Permission createPermission(User user, Paste paste, PermissionType permissionType) {
        Permission permission = new Permission(user,paste);
        permission.setId(permissionCounter++);
        permission.setType(permissionType);
        permission.setCreatedAt(LocalDateTime.now());
        return permission;
    }
}