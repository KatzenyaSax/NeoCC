package com.dafuweng.auth.filter;

import com.dafuweng.auth.entity.SysUserEntity;
import com.dafuweng.auth.service.SysUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SysUserService sysUserService;

    public JwtAuthenticationFilter(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 放行公开路径（doc04 修正后：Gateway StripPrefix=0，/auth/login 完整转发到 auth:8085/auth/login）
        if (path.contains("/api/sysUser/login") || path.contains("/api/sysUser/page") ||
            path.equals("/auth/login") || path.startsWith("/auth/login") ||
            path.equals("/auth/getInfo") || path.startsWith("/auth/getInfo") ||
            path.equals("/auth/getRouters") || path.startsWith("/auth/getRouters") ||
            path.equals("/auth/logout") || path.startsWith("/auth/logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 当前设计：token = userId 字符串（与 Plan08 Gateway AuthFilter 一致）
            Long userId = Long.parseLong(token);

            // 加载用户信息
            SysUserEntity user = sysUserService.getById(userId);
            if (user == null || Objects.equals(user.getDeleted(), (short) 1)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 加载角色和权限码
            List<String> roleIds = sysUserService.getRoleIdsByUserId(userId)
                    .stream().map(String::valueOf).collect(Collectors.toList());
            List<String> permCodes = sysUserService.getPermCodesByUserId(userId);

            List<SimpleGrantedAuthority> authorities = roleIds.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toList());
            authorities.addAll(permCodes.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            // token 解析失败或用户不存在，静默放行让后续 SecurityFilterChain 处理
        }

        filterChain.doFilter(request, response);
    }
}
