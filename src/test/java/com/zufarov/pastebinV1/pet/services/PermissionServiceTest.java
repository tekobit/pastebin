package com.zufarov.pastebinV1.pet.services;

import com.redis.testcontainers.RedisContainer;
import com.zufarov.pastebinV1.pet.components.AuthenticationFacade;
import com.zufarov.pastebinV1.pet.dtos.PermissionDto;
import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.Permission;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.repositories.PastesRepository;
import com.zufarov.pastebinV1.pet.repositories.PermissionsRepository;
import com.zufarov.pastebinV1.pet.repositories.UsersRepository;
import com.zufarov.pastebinV1.pet.util.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.junit.jupiter.api.function.Executable;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
class PermissionServiceTest {

    @Autowired
    private PermissionService permissionService;

    @SpyBean
    private UsersRepository usersRepository;

    @Autowired
    @SpyBean
    private PermissionsRepository permissionsRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private PastesRepository pastesRepository;

    @MockBean
    AuthenticationFacade authenticationFacade;

    // postgres
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");
    @Autowired
    private CacheService cacheService;

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


    private final String NON_EXISTING_USER_NAME = "non_existing_user";
    private final String NON_EXISTING_PASTE_ID = "non_existing_paste_id";

    private User owner;
    private User editor;
    private User grantedUser;
    private Paste paste;

    private long countTotalPermissions;

    @BeforeEach
    void setUp() {
        permissionsRepository.deleteAll();
        pastesRepository.deleteAll();
        usersRepository.deleteAll();
        cacheManager.getCacheNames().forEach(e -> cacheManager.getCache(e).clear());

        owner = createAndSaveUser("owner", "owner@mail.com");
        editor = createAndSaveUser("editor", "editor@mail.com");
        grantedUser = createAndSaveUser("granted","granted@mail.com");
        paste = createAndSavePaste(owner);
        createAndSavePermission(owner, paste, PermissionType.OWNER);
        createAndSavePermission(editor, paste, PermissionType.EDITOR);

    }

    @Nested
    class SavePermissionTests {
        @Nested
        class SavePermissionSuccessfulTests {

            @MethodSource("com.zufarov.pastebinV1.pet.services.PermissionServiceTest#getPermissionTypes")
            @ParameterizedTest
            void savePermission_ShouldSavePermission_WhenUserIsOwnerAndPermissionDoesNotExist(PermissionType permissionType) {
                setAuthentication(owner.getName());

                PermissionDto permissionDto = new PermissionDto(permissionType,paste.getId(),grantedUser.getName());

                permissionService.savePermission(permissionDto);

                Permission receivedPermission = permissionsRepository.findByPasteAndUser(paste,grantedUser).get();

                assertEquals(permissionType, receivedPermission.getType());
            }

        }
        @Nested
        class SavePermissionFailedTests {
            // user has permission, but he is not owner
            @Test
            void savePermission_ShouldThrowForbiddenException_WhenUserIsNotOwner() {
                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER,paste.getId(),grantedUser.getName());

                assertForbiddenForNotOwner(() -> permissionService.savePermission(permissionDto));
            }

            // tests connected with findPermission
            @Test
            void savePermission_ShouldThrowNotFoundException_WhenOwnerDoesNotExist()     {
                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER, paste.getId(), grantedUser.getName());

                assertNotFoundForNotExistingOwner(() -> permissionService.savePermission(permissionDto));
            }

            @Test
            void savePermission_ShouldThrowNotFoundException_WhenPasteDoesNotExist() {
                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER, NON_EXISTING_PASTE_ID,grantedUser.getName());

                assertNotFoundForNotExistingPaste(() -> permissionService.savePermission(permissionDto));
            }

            //// user does not have permissions to paste at all
            @Test
            void savePermission_ShouldThrowNotFoundException_WhenOwnerDoesNotHavePermission() {
                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER, paste.getId(), grantedUser.getName());

                assertNotFoundForOwnerWithoutPermission(() -> permissionService.savePermission(permissionDto));
            }

            // tests connected with checkIfPermissionAlreadyExists
            @Test
            void savePermission_ShouldThrowNotFoundException_WhenGrantingUserDoesNotExist() {
                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER,paste.getId(), NON_EXISTING_USER_NAME);

                assertNotFoundForNotExistingGrantingUser(() -> permissionService.savePermission(permissionDto));
            }

            @Test
            void savePermission_ShouldThrowBadRequestException_WhenPermissionAlreadyExists() {
                setAuthentication(owner.getName());
                createAndSavePermission(grantedUser,paste,PermissionType.VIEWER);

                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER, paste.getId(), grantedUser.getName());

                countTotalPermissions = permissionsRepository.count();
                assertThrows(BadRequestException.class,() -> permissionService.savePermission(permissionDto));
                assertEquals(countTotalPermissions,permissionsRepository.count());
            }


        }
    }

    @Nested
    class EditPermissionTests {
        @Nested
        class EditPermissionSuccessfulTests {

            @MethodSource("com.zufarov.pastebinV1.pet.services.PermissionServiceTest#getPermissionTypes")
            @ParameterizedTest
            void editPermission_ShouldEditPermission_WhenUserIsOnlyOwnerAndUserNotEditingOwnPermission(PermissionType permissionType) {
                setAuthentication(owner.getName());
                createAndSavePermission(grantedUser,paste,PermissionType.VIEWER);

                PermissionDto permissionDto = new PermissionDto(permissionType,paste.getId(),grantedUser.getName());

                permissionService.editPermission(permissionDto);

                Permission receivedPermission = permissionsRepository.findByPasteAndUser(paste,grantedUser).get();

                assertEquals(permissionType, receivedPermission.getType());
            }

            @Test
            void editPermission_ShouldEditPermission_WhenMultipleOwnersExistAndOneEditsAnother() {
                setAuthentication(owner.getName());
                createAndSavePermission(grantedUser,paste,PermissionType.OWNER);

                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER,paste.getId(),grantedUser.getName());

                permissionService.editPermission(permissionDto);
                Permission receivedPermission = permissionsRepository.findByPasteAndUser(paste,grantedUser).get();


                assertEquals(permissionDto.type(), receivedPermission.getType());
            }

            @Test
            void editPermission_ShouldUpdateCache_WhenPermissionIsSuccessfullyEdited() {
                setAuthentication(owner.getName());

                Permission createdPermission = createAndSavePermission(grantedUser,paste,PermissionType.VIEWER);
                cacheManager.getCache("permissionCache").put(createdPermission.getPaste().getId()+"_"+createdPermission.getUser().getName(), createdPermission);

                PermissionDto permissionDto = new PermissionDto(PermissionType.EDITOR,paste.getId(),grantedUser.getName());
                permissionService.editPermission(permissionDto);

                assertEquals(cacheManager.getCache("permissionCache")
                        .get(createdPermission.getPaste().getId()+"_"+createdPermission.getUser().getName(),Permission.class).getType(),permissionDto.type());
            }
        }
        @Nested
        class EditPermissionFailedTests {
            @Test
            void editPermission_ShouldThrowForbiddenException_WhenUserIsNotOwner() {
                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER,paste.getId(),grantedUser.getName());

                assertForbiddenForNotOwner(() -> permissionService.editPermission(permissionDto));
            }

            @Test
            void editPermission_ShouldThrowNotFoundException_WhenOwnerDoesNotExist()     {
                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER, paste.getId(), grantedUser.getName());

                assertForbiddenForNotOwner(() -> permissionService.editPermission(permissionDto));
            }

            @Test
            void editPermission_ShouldThrowNotFoundException_WhenPasteDoesNotExist() {
                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER, NON_EXISTING_PASTE_ID,grantedUser.getName());

                assertNotFoundForNotExistingPaste(() -> permissionService.editPermission(permissionDto));
            }

            @Test
            void savePermission_ShouldThrowNotFoundException_WhenOwnerDoesNotHavePermission() {
                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER, paste.getId(), grantedUser.getName());

                assertNotFoundForOwnerWithoutPermission(() -> permissionService.editPermission(permissionDto));
            }

            @Test
            void editPermission_ShouldThrowNotFoundException_WhenGrantingUserDoesNotExist() {
                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER,paste.getId(), NON_EXISTING_USER_NAME);

                assertNotFoundForNotExistingGrantingUser(() -> permissionService.editPermission(permissionDto));
            }


            @Test
            void editPermission_ShouldThrowNotFoundException_WhenGrantingUserDoesNotHavePermission() {
                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER,paste.getId(), grantedUser.getName());

                assertNotFoundForNotExistingPermissionOfGrantingUser(() -> permissionService.editPermission(permissionDto));
            }


            @Test
            void editPermission_ShouldThrowBadRequestException_WhenUserIsOnlyOwnerAndUserEditsOwnPermission() {
                setAuthentication(owner.getName());

                PermissionDto permissionDto = new PermissionDto(PermissionType.EDITOR,paste.getId(),owner.getName());

                countTotalPermissions = permissionsRepository.count();
                assertThrows(BadRequestException.class,()-> permissionService.editPermission(permissionDto) );
                assertEquals(countTotalPermissions,permissionsRepository.count());
            }

        }

    }

    @Nested
    class DeletePermissionTests {

        @Nested
        class DeletePermissionSuccessfulTests {

            @MethodSource("com.zufarov.pastebinV1.pet.services.PermissionServiceTest#getPermissionTypes")
            @ParameterizedTest
            void deletePermission_ShouldDeletePermission_WhenOwnerDeletesOtherPermission(PermissionType permissionType) {
                setAuthentication(owner.getName());
                createAndSavePermission(grantedUser,paste,permissionType);

                PermissionDto permissionDto = new PermissionDto(permissionType,paste.getId(),grantedUser.getName());
                permissionService.deletePermission(permissionDto);

                assertThrows(NoSuchElementException.class,() -> permissionsRepository.findByPasteAndUser(paste,grantedUser).get());
            }

            @Test
            void deletePermission_ShouldEvictPermissionFromCache_WhenOwnerSuccessfullyDeletesPermission() {
                setAuthentication(owner.getName());

                Permission createdPermission = createAndSavePermission(grantedUser,paste,PermissionType.VIEWER);
                cacheService.putPermissionToCache(createdPermission);

                PermissionDto permissionDto = new PermissionDto(createdPermission.getType(),paste.getId(),grantedUser.getName());
                permissionService.deletePermission(permissionDto);

                assertThrows(NoSuchElementException.class,() -> permissionsRepository.findByPasteAndUser(paste,grantedUser).get());
            }
        }

        @Nested
        class DeletePermissionFailedTests {
            @Test
            void deletePermission_ShouldThrowForbiddenException_WhenUserIsNotOwner() {
                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER,paste.getId(),grantedUser.getName());

                assertForbiddenForNotOwner(() -> permissionService.deletePermission(permissionDto));
            }

            @Test
            void deletePermission_ShouldThrowNotFoundException_WhenOwnerDoesNotExist() {
                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER, paste.getId(), grantedUser.getName());

                assertForbiddenForNotOwner(() -> permissionService.deletePermission(permissionDto));
            }

            @Test
            void deletePermission_ShouldThrowNotFoundException_WhenPasteDoesNotExist() {
                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER, NON_EXISTING_PASTE_ID,grantedUser.getName());

                assertNotFoundForNotExistingPaste(() -> permissionService.deletePermission(permissionDto));
            }

            @Test
            void deletePermission_ShouldThrowNotFoundException_WhenOwnerDoesNotHavePermission() {
                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER, paste.getId(), grantedUser.getName());

                assertNotFoundForOwnerWithoutPermission(() -> permissionService.deletePermission(permissionDto));
            }

            @Test
            void deletePermission_ShouldThrowForbiddenException_WhenOnlyOwnerDeletesOwnPermission() {
                setAuthentication(owner.getName());

                PermissionDto permissionDto = new PermissionDto(PermissionType.OWNER, paste.getId(),owner.getName());

                assertThrows(ForbiddenException.class,() -> permissionService.deletePermission(permissionDto));

            }


            @Test
            void deletePermission_ShouldThrowNotFoundException_WhenGrantingUserDoesNotExist() {
                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER,paste.getId(), NON_EXISTING_USER_NAME);

                assertNotFoundForNotExistingGrantingUser(() -> permissionService.deletePermission(permissionDto));
            }

            @Test
            void deletePermission_ShouldThrowNotFoundException_WhenGrantingUserDoesNotHavePermission() {
                PermissionDto permissionDto = new PermissionDto(PermissionType.VIEWER,paste.getId(), grantedUser.getName());

                assertNotFoundForNotExistingPermissionOfGrantingUser(() -> permissionService.deletePermission(permissionDto));
            }
        }
    }

    @Nested
    class AddOwnerTests {
        @Test
        void addOwner_ShouldAddOwner_WhenWhenValidInput() {
            permissionService.addOwner(grantedUser,paste);

            assertEquals(PermissionType.OWNER,permissionsRepository.findByPasteAndUser(paste,grantedUser).get().getType());
        }
        @Test
        void addOwner_ShouldPutPermissionToCache_WhenOwnerSuccessfullyAdded() {
            permissionService.addOwner(grantedUser,paste);

            assertNotNull(cacheManager.getCache("permissionCache").get(PermissionCacheKeyGenerator.generateCacheKey(paste.getId(),grantedUser.getName())));
        }
    }

    private static Stream<PermissionType> getPermissionTypes() {
        return Stream.of(PermissionType.values());
    }

    private void setAuthentication(String username) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        when(authenticationFacade.getAuthentication()).thenReturn(authentication);
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
        permission.setType(permissionType);
        permissionsRepository.save(permission);
        return permission;
    }


    private void assertNotFoundForNotExistingPermissionOfGrantingUser(Executable executable)  {
        setAuthentication(owner.getName());

        countTotalPermissions = permissionsRepository.count();
        assertThrows(NotFoundException.class,executable);
        assertEquals(countTotalPermissions,permissionsRepository.count());
    }

    private void assertNotFoundForNotExistingGrantingUser(Executable executable) {
        setAuthentication(owner.getName());

        countTotalPermissions = permissionsRepository.count();
        assertThrows(NotFoundException.class,executable);
        assertEquals(countTotalPermissions,permissionsRepository.count());
    }

    private void assertNotFoundForOwnerWithoutPermission(Executable executable) {
        setAuthentication(owner.getName());
        when(permissionsRepository.findByPasteAndUser(paste,owner)).thenReturn(Optional.empty());


        countTotalPermissions = permissionsRepository.count();
        assertThrows(NotFoundException.class,executable);
        assertEquals(countTotalPermissions,permissionsRepository.count());
    }

    private void assertNotFoundForNotExistingPaste(Executable executable) {
        setAuthentication(owner.getName());

        countTotalPermissions = permissionsRepository.count();
        assertThrows(NotFoundException.class,executable);
        assertEquals(countTotalPermissions,permissionsRepository.count());
    }

    private void assertNotFoundForNotExistingOwner(Executable executable) {
        setAuthentication(NON_EXISTING_USER_NAME);

        countTotalPermissions = permissionsRepository.count();
        assertThrows(NotFoundException.class,executable);
        assertEquals(countTotalPermissions,permissionsRepository.count());
    }

    private void assertForbiddenForNotOwner (Executable executable) {
        setAuthentication(editor.getName());

        countTotalPermissions = permissionsRepository.count();
        assertThrows(ForbiddenException.class,executable);
        assertEquals(countTotalPermissions,permissionsRepository.count());
    }
}