package com.dafuweng.sales.feign;

import com.dafuweng.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "dafuweng-auth", contextId = "authClientForSales")
public interface AuthFeignClient {

    /**
     * 根据用户ID查询用户信息
     */
    @GetMapping("/api/sysUser/{id}")
    Result<?> getUserById(@PathVariable Long id);

    /**
     * 查询用户的权限码列表（业务操作前鉴权）
     */
    @GetMapping("/api/sysUser/{id}/permCodes")
    Result<List<String>> getPermCodes(@PathVariable Long id);
}
