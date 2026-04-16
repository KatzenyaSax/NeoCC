package com.dafuweng.auth.filter;

import com.dafuweng.auth.entity.SysUserEntity;
import com.dafuweng.auth.service.SysUserService;
import com.dafuweng.auth.utils.JwtUtil;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器
 * 
 * 支持真正的JWT Token验证：
 * - Token签名验证
 * - Token过期检查
 * - 解析Token获取用户信息和权限
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SysUserService sysUserService;
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(SysUserService sysUserService, JwtUtil jwtUtil) {
        this.sysUserService = sysUserService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 放行公开路径
        if (path.contains("/api/sysUser/login") || path.contains("/api/sysUser/page")
            || path.contains("/login") || path.contains("/captchaImage")) {
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
            // 验证Token是否有效
            if (!jwtUtil.validateToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 检查Token是否过期
            if (jwtUtil.isTokenExpired(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 解析Token获取用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            String[] roles = jwtUtil.getRolesFromToken(token);
            String[] perms = jwtUtil.getPermsFromToken(token);

            // 加载用户信息
            SysUserEntity user = sysUserService.getById(userId);
            if (user == null || Objects.equals(user.getDeleted(), (short) 1)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 构建权限列表
            List<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toList());
            authorities.addAll(Arrays.stream(perms).map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            // token 解析失败或用户不存在，静默放行让后续 SecurityFilterChain 处理
            logger.debug("JWT Token validation failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
