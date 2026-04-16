package com.dafuweng.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.dafuweng.auth.filter.JwtAuthenticationFilter;
import com.dafuweng.auth.service.SysUserService;
import com.dafuweng.auth.utils.JwtUtil;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final SysUserService sysUserService;
    private final JwtUtil jwtUtil;

    public SecurityConfig(SysUserService sysUserService, JwtUtil jwtUtil) {
        this.sysUserService = sysUserService;
        this.jwtUtil = jwtUtil;
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
                // 系统管理接口（用户/角色/权限管理）
                .requestMatchers("/api/sysUser/**", "/api/sysRole/**", "/api/sysPermission/**").permitAll()
                // Token刷新接口白名单
                .requestMatchers("/token/**", "/api/token/**").permitAll()
                // OAuth2 二维码登录接口白名单
                .requestMatchers("/api/oauth2/qrcode/**").permitAll()
                // RuoYi 前端适配接口白名单
                .requestMatchers("/captchaImage", "/login", "/logout", "/register", "/getInfo", "/getRouters", "/unlockscreen").permitAll()
                // Swagger API文档
                .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/swagger-resources/**", "/webjars/**").permitAll()
                .requestMatchers("/static/**", "/favicon.ico").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(sysUserService, jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
