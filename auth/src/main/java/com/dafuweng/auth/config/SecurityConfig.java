package com.dafuweng.auth.config;

import com.dafuweng.auth.filter.JwtAuthenticationFilter;
import com.dafuweng.auth.service.SysUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final SysUserService sysUserService;

    public SecurityConfig(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .logout(logout -> logout.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/sysUser/login", "/api/sysUser/page").permitAll()
                .requestMatchers("/api/sysUser/dev/**").permitAll()
                // RuoYi 前端适配接口白名单
                .requestMatchers("/captchaImage", "/login", "/logout", "/register", "/getInfo", "/getRouters", "/unlockscreen").permitAll()
                .requestMatchers("/static/**", "/favicon.ico").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(sysUserService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
