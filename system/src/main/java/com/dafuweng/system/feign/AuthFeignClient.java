package com.dafuweng.system.feign;

import com.dafuweng.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "auth", contextId = "authClientForSystem", url = "http://localhost:8085")
public interface AuthFeignClient {

    /**
     * 根据用户ID列表查询用户真实姓名
     */
    @PostMapping("/api/sysUser/names/by-ids")
    Result<Map<Long, String>> listUserNamesByIds(@RequestBody List<Long> ids);
}