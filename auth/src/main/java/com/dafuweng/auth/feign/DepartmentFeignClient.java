package com.dafuweng.auth.feign;

import com.dafuweng.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/**
 * 调用 system 服务的 Feign 客户端
 */
@FeignClient(name = "system", url = "${feign.system.url:http://neocc-system:8082}")
public interface DepartmentFeignClient {

    /**
     * 获取所有部门列表（用于填充部门名称）
     * 返回 Map 以避免跨模块实体类依赖
     */
    @GetMapping("/api/sysDepartment/listAll")
    Result<List<Map<String, Object>>> listAll();
}
