package com.dafuweng.auth.feign;

import com.dafuweng.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "system", contextId = "systemClientForAuth", url = "http://localhost:8082")
public interface SystemFeignClient {

    /**
     * 根据部门ID列表查询部门名称
     */
    @PostMapping("/api/sysDepartment/names/by-ids")
    Result<Map<Long, String>> listDeptNamesByIds(@RequestBody List<Long> ids);
}