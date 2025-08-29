package com.zufarov.pastebinV1.pet.services;

import com.redis.testcontainers.RedisContainer;
import com.zufarov.pastebinV1.pet.components.AuthenticationFacade;
import com.zufarov.pastebinV1.pet.dtos.PasteRequestDto;
import com.zufarov.pastebinV1.pet.dtos.PasteUpdateDto;
import com.zufarov.pastebinV1.pet.mappers.PermissionMapperService;
import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.Permission;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.repositories.PastesRepository;
import com.zufarov.pastebinV1.pet.repositories.PermissionsRepository;
import com.zufarov.pastebinV1.pet.repositories.UsersRepository;
import com.zufarov.pastebinV1.pet.security.CustomUserDetails;
import com.zufarov.pastebinV1.pet.util.ForbiddenException;
import com.zufarov.pastebinV1.pet.util.NotFoundException;
import com.zufarov.pastebinV1.pet.util.PermissionType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class DataBaseServiceTest {
    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private DataBaseService dataBaseService;

    @Autowired
    private PermissionsRepository permissionsRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private PastesRepository pastesRepository;

    @Autowired
    private CacheService cacheService;

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

    // configure mock reps

    //// amazon s3
//    static DockerImageName localStackImage = DockerImageName.parse("localstack/localstack:latest");
//
//    @Container
//    public static LocalStackContainer localstack = new LocalStackContainer(localStackImage).withServices(LocalStackContainer.Service.S3);
//
//    static S3Client s3;
//    @BeforeAll
//    static void setup() {
//        s3 = S3Client
//                .builder()
//                .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.S3))
//                .credentialsProvider(
//                        StaticCredentialsProvider.create(
//                                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
//                        )
//                )
//                .region(Region.of(localstack.getRegion()))
//                .build();
//
//    }

    private User owner;
    private User editor;
    private User viewer;
    private User random;
    private Paste privatePaste;
    private Paste publicPaste;

    @BeforeEach
    void setup() {
        permissionsRepository.deleteAll();
        pastesRepository.deleteAll();
        usersRepository.deleteAll();
        cacheManager.getCacheNames().forEach(e -> cacheManager.getCache(e).clear());

        // 1. Arrange - создаем всех участников и пасту один раз для всех тестов в этом классе
        owner = createAndSaveUser("owner", "owner@mail.com");
        editor = createAndSaveUser("editor", "editor@mail.com");
        viewer = createAndSaveUser("viewer", "viewer@mail.com");
        random = createAndSaveUser("random", "random@mail.com");

        privatePaste = createAndSavePrivatePaste(owner); // Хелпер для private пасты
        publicPaste = createAndSavePublicPaste(owner); // Хелпер для private пасты


        // Назначаем права
        createAndSavePermission(owner, privatePaste, PermissionType.OWNER);
        createAndSavePermission(editor, privatePaste, PermissionType.EDITOR);
        createAndSavePermission(viewer, privatePaste, PermissionType.VIEWER);

        createAndSavePermission(owner, publicPaste, PermissionType.OWNER);
        createAndSavePermission(editor, publicPaste, PermissionType.EDITOR);
        createAndSavePermission(viewer, publicPaste, PermissionType.VIEWER);

    }

    @MockBean
    AuthenticationFacade authenticationFacade;

    @MockBean
    CustomUserDetailService customUserDetailService;

    @Nested
    class savePasteMetadataTests {
        @Test
        void savePasteMetadata() {
            setAuthentication(owner.getName());
            setCustomUserDetails(owner);

            PasteRequestDto paste = new PasteRequestDto("name","public",java.time.LocalDateTime.now().plusDays(1),"content","customId");
            assertDoesNotThrow(() -> dataBaseService.savePasteMetadata(paste,paste.customId(),"url"));


            Paste savedPaste = pastesRepository.findById(paste.customId()).get();

            assertNotNull(savedPaste);
            assertThat(savedPaste.getTitle()).isEqualTo(paste.title());
            assertThat(savedPaste.getVisibility()).isEqualTo(paste.visibility());
            assertThat(savedPaste.getOwner()).isEqualTo(owner);
            assertThat(savedPaste.getLastVisited()).isEqualTo(savedPaste.getCreatedAt());
            assertThat(savedPaste.getId()).isEqualTo(paste.customId());
            assertThat(savedPaste.getContentLocation()).isEqualTo("url");
            assertNotNull(cacheManager.getCache("pasteMetadataCache").get(paste.customId()));

            assertThat(savedPaste.getExpiresAt()).isCloseTo(
                    paste.expiresAt(),
                    within(1, ChronoUnit.MILLIS)
            );
            assertThat(savedPaste.getCreatedAt()).isCloseTo(
                    LocalDateTime.now(),
                    within(1, ChronoUnit.SECONDS)
            );
        }
    }

    @Nested
    class GetPublicPasteMetadataTests {
        @Test
        void getPasteMetadata_anonymousUser_success() {
            when(authenticationFacade.getAuthentication()).thenReturn(null);

            Paste result = assertDoesNotThrow(() -> dataBaseService.getPasteMetadata(publicPaste.getId()));

            assertThat(result).isEqualTo(publicPaste);
            assertNotNull( cacheManager.getCache("pasteMetadataCache").get(publicPaste.getId()) );
        }

        @Test
        void getPasteMetadata_pasteNotFound() {
            assertThrows(NotFoundException.class,() -> dataBaseService.getPasteMetadata("pasteNotFound"));
        }
    }

    @Nested
    class GetPrivatePasteMetadataTests  {

        static Stream<String> authorizerUsersProvider() {
            return Stream.of("owner", "editor", "viewer");
        }

        @ParameterizedTest
        @MethodSource("authorizerUsersProvider")
        void getPrivatePasteMetadata_usersWithAccessRoots(String username) {
            setAuthentication(username);

            Paste savedPaste = assertDoesNotThrow(() -> dataBaseService.getPasteMetadata(privatePaste.getId()) );

            assertThat(savedPaste).isEqualTo(privatePaste);
            assertNotNull(  cacheManager.getCache("pasteMetadataCache").get(privatePaste.getId()));

        }


        @Test
        void getPrivatePasteMetadata_usersWithOutAccessRoots_throwsForbiddenException() {
            setAuthentication(random.getName());

            assertThrows(ForbiddenException.class,() -> dataBaseService.getPasteMetadata(privatePaste.getId()));

        }
    }

    @Nested
    class DeletePasteMetadataTests {
        @Test
        void deletePasteMetadataWithSuccess_owner() {
            cacheService.putPasteMetadataToCache( publicPaste,publicPaste.getId());

            setAuthentication(owner.getName());
            assertDoesNotThrow( () -> dataBaseService.deletePasteMetadata(publicPaste.getId()) );

            assertThrows(NoSuchElementException.class,() ->pastesRepository.findById(publicPaste.getId()).get());
            assertNull(cacheManager.getCache("pasteMetadataCache").get(publicPaste.getId()));

        }

        static Stream<String> usersWithoutNecessaryRoots() {
            return Stream.of("editor", "viewer","random");
        }

        @ParameterizedTest
        @MethodSource("usersWithoutNecessaryRoots")
        void deletePasteMetadataWithoutSuccess_others(String username) {
            cacheService.putPasteMetadataToCache( publicPaste,publicPaste.getId());

            setAuthentication(username);

            assertThrows(ForbiddenException.class,() ->dataBaseService.deletePasteMetadata(publicPaste.getId()));
            assertNotNull(pastesRepository.findById(publicPaste.getId()).get());
            assertNotNull(cacheManager.getCache("pasteMetadataCache").get(publicPaste.getId()));

        }

        @Test
        void deletePasteMetadata_pasteNotFound() {
            assertThrows(NotFoundException.class,() -> dataBaseService.deletePasteMetadata("pasteNotFound"));
        }


    }

    @Nested
    class UpdatePasteMetadataTests {

        static Stream<String> usersWithNecessaryRoots() {
            return Stream.of("owner", "editor");
        }

        @ParameterizedTest
        @MethodSource("usersWithNecessaryRoots")
        void updatePasteMetadata_success_userHasAccessRoots(String username) {
            cacheService.putPasteMetadataToCache( publicPaste,publicPaste.getId());

            setAuthentication(username);

            PasteUpdateDto pasteToUpdate = new PasteUpdateDto("new_title","private",publicPaste.getExpiresAt().plusDays(10).truncatedTo(ChronoUnit.MICROS),"new_content");
            assertDoesNotThrow(() -> dataBaseService.updatePasteMetadata(pasteToUpdate,publicPaste.getId()));

            Paste pasteAfterUpdate = pastesRepository.findById(publicPaste.getId()).get();

            assertNotNull(pasteAfterUpdate);
            assertThat(pasteAfterUpdate.getTitle()).isEqualTo(pasteToUpdate.title());
            assertThat(pasteAfterUpdate.getVisibility()).isEqualTo(pasteToUpdate.visibility());
            assertThat(pasteAfterUpdate.getExpiresAt()).isCloseTo(pasteToUpdate.expiresAt(), within(1, ChronoUnit.MILLIS));
            assertThat(cacheManager.getCache("pasteMetadataCache").get(publicPaste.getId()).get()).usingRecursiveComparison().ignoringFields("owner","permissions").isEqualTo(pasteAfterUpdate);

        }

        static Stream<String> usersWithoutNecessaryRoots() {
            return Stream.of("viewer", "random");
        }

        @ParameterizedTest
        @MethodSource("usersWithoutNecessaryRoots")
        void updatePasteMetadata_failure_userHasNoAccessRoots(String username) {
            cacheService.putPasteMetadataToCache( publicPaste,publicPaste.getId());

            setAuthentication(username);

            PasteUpdateDto pasteToUpdate = new PasteUpdateDto("new_title","private",publicPaste.getExpiresAt().plusDays(10).truncatedTo(ChronoUnit.MICROS),"new_content");
            assertThrows(ForbiddenException.class , () -> dataBaseService.updatePasteMetadata(pasteToUpdate,publicPaste.getId()));

            Paste pasteAfterUpdate = pastesRepository.findById(publicPaste.getId()).get();

            assertNotNull(pasteAfterUpdate);
            assertThat(pasteAfterUpdate.getTitle()).isEqualTo(publicPaste.getTitle());
            assertThat(pasteAfterUpdate.getVisibility()).isEqualTo(publicPaste.getVisibility());
            assertThat(pasteAfterUpdate.getExpiresAt()).isCloseTo(publicPaste.getExpiresAt(), within(1, ChronoUnit.MILLIS));
            assertThat(cacheManager.getCache("pasteMetadataCache").get(publicPaste.getId()).get())
                    .usingRecursiveComparison().ignoringFields("owner","permissions")
                    .isEqualTo(pasteAfterUpdate);
        }

        @Test
        void updatePasteMetadata_failure_PasteNotFound() {
            PasteUpdateDto pasteToUpdate = new PasteUpdateDto("new_title","private",publicPaste.getExpiresAt().plusDays(10).truncatedTo(ChronoUnit.MICROS),"new_content");
            assertThrows(NotFoundException.class,() -> dataBaseService.updatePasteMetadata(pasteToUpdate,"wrong_id"));
        }

        }

    private void setAuthentication(String username) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        when(authenticationFacade.getAuthentication()).thenReturn(authentication);
    }

    private void setCustomUserDetails(User user) {
        CustomUserDetails customUserDetails = mock(CustomUserDetails.class);
        when(customUserDetails.getUser()).thenReturn(user);
        when(customUserDetailService.loadUserByUsername(anyString())).thenReturn(customUserDetails);
    }

    private @NotNull User createAndSaveUser(String name, String email) {
        User user = new User(name,email,"password", LocalDateTime.now(), LocalDateTime.now(),"ROLE_USER");
        usersRepository.save(user);
        return user;
    }

    private @NotNull Paste createAndSavePublicPaste(User user) {
        Paste paste = new Paste();
        paste.setId("public-id");
        paste.setTitle("public-title");
        paste.setContentLocation("location");
        paste.setCreatedAt(LocalDateTime.now());
        paste.setExpiresAt(LocalDateTime.now().plusDays(15));
        paste.setVisibility("public");
        paste.setLastVisited(LocalDateTime.now());
        paste.setOwner(user);
        pastesRepository.save(paste);
        return paste;
    }

    private @NotNull Paste createAndSavePrivatePaste(User user) {
        Paste paste = new Paste();
        paste.setId("private-id");
        paste.setTitle("private-title");
        paste.setContentLocation("location");
        paste.setCreatedAt(LocalDateTime.now());
        paste.setExpiresAt(LocalDateTime.now().plusDays(15));
        paste.setVisibility("private");
        paste.setLastVisited(LocalDateTime.now());
        paste.setOwner(user);

        pastesRepository.save(paste);
        return paste;
    }

    private void createAndSavePermission(User user, Paste paste, PermissionType permissionType) {
        Permission permission = new Permission(user,paste);
        permission.setType(permissionType.name());
        permissionsRepository.save(permission);
    }
}