package com.zufarov.pastebinV1.pet.config;

import com.zufarov.pastebinV1.pet.services.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final UserDetailService userDetailService;
    @Autowired
    public SecurityConfig(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(request -> request.requestMatchers("/auth/registration","/error").permitAll()
                .anyRequest().hasAnyRole("USER","ADMIN"))

//                  .anyRequest().permitAll())
                .formLogin(form -> form
                        .permitAll()
                        .loginPage("/auth/login")
                        .defaultSuccessUrl("/profile",true)
                        .failureUrl("/auth/login"))
                .httpBasic(Customizer.withDefaults())
                .userDetailsService(userDetailService)
                .passwordManagement( pass -> getPasswordEncoder())
                .logout(lg -> lg.logoutUrl("/logout").logoutSuccessUrl("/auth/login"))
                .csrf(csrf -> csrf.disable())
                .build();
    }
    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
