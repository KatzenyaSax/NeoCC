package com.dafuweng.finance.feign;

import com.dafuweng.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "auth", contextId = "authClientForFinance", url = "http://localhost:8085")
public interface AuthFeignClient {

    /**
     * 根据用户ID查询用户信息
     */
    @GetMapping("/api/sysUser/{id}")
    Result<?> getUserById(@PathVariable Long id);

    /**
     * 获取销售代表列表
     */
    @GetMapping("/api/sysUser/sales-reps")
    Result<List<Map<String, Object>>> listSalesReps();
}
