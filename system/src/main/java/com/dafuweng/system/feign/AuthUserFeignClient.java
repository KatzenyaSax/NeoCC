package com.dafuweng.system.feign;

import com.dafuweng.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * 调用 auth 服务的 Feign 客户端
 */
@FeignClient(name = "auth", url = "${feign.auth.url:http://localhost:8085}")
public interface AuthUserFeignClient {

    /**
     * 获取用户详情
     */
    @GetMapping("/api/sysUser/{id}")
    Result<Map<String, Object>> getUserById(@PathVariable("id") Long id);

    /**
     * 获取所有销售代表列表
     */
    @GetMapping("/api/sysUser/sales-reps")
    Result<List<Map<String, Object>>> listSalesReps();
}
