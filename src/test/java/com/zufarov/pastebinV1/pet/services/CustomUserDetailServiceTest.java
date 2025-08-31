package com.zufarov.pastebinV1.pet.services;

import com.redis.testcontainers.RedisContainer;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.repositories.UsersRepository;
import com.zufarov.pastebinV1.pet.security.CustomUserDetails;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Testcontainers
@SpringBootTest
class CustomUserDetailServiceTest {

    @SpyBean
    UsersRepository usersRepository;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    CustomUserDetailService customUserDetailService;

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
        usersRepository.deleteAll();
        cacheManager.getCacheNames().forEach(e -> cacheManager.getCache(e).clear());
    }


    @Nested
    class loadUserByUsernameTests {
        @Test
        void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
            User user = createAndSaveUser("name","email");

            CustomUserDetails customUserDetails = customUserDetailService.loadUserByUsername(user.getName());
            customUserDetailService.loadUserByUsername(user.getName());
            assertThat(customUserDetails.getUser()).isEqualTo(new CustomUserDetails(user).getUser()  );
            assertThat(cacheManager.getCache("userCache").get(user.getName(),CustomUserDetails.class).getUser()).isEqualTo(user);

            verify(usersRepository,times(1)).findByName(user.getName());
        }
        @Test
        void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUserDoesNotExist() {
            assertThrows(UsernameNotFoundException.class,() -> customUserDetailService.loadUserByUsername("not_existing_name"));
        }
    }

    @Nested
    class DeleteUserTests {
        @Test
        void deleteUser_ShouldDeleteUser_WhenUserExists() {
            User user = createAndSaveUser("name","email");
            cacheManager.getCache("userCache").put(user.getName(),new CustomUserDetails(user));

            customUserDetailService.deleteUser(user.getName());

            assertThat(usersRepository.findById(user.getId()).isEmpty()).isTrue();
            assertNull(cacheManager.getCache("userCache").get(user.getName()));
        }


    }


    private @NotNull User createAndSaveUser(String name, String email) {
        User user = new User(name,email,"password", LocalDateTime.now(), LocalDateTime.now(),"ROLE_USER");
        usersRepository.save(user);
        return user;
    }
}