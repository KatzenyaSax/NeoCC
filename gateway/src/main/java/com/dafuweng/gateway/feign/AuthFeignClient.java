package com.dafuweng.gateway.feign;

import com.dafuweng.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "dafuweng-auth", contextId = "authClient")
public interface AuthFeignClient {

    /**
     * 根据用户ID查询用户信息（验证 Token 对应的用户是否存在且有效）
     * gateway 在 Token 校验时调用
     */
    @GetMapping("/auth/api/sysUser/{id}")
    Result<?> getUserById(@PathVariable Long id);

    /**
     * 查询用户的角色ID列表
     */
    @GetMapping("/auth/api/sysUser/{id}/roles")
    Result<?> getRoleIds(@PathVariable Long id);

    /**
     * 查询用户的权限码列表
     */
    @GetMapping("/auth/api/sysUser/{id}/permCodes")
    Result<?> getPermCodes(@PathVariable Long id);
}
