package com.dafuweng.sales.feign;

import com.dafuweng.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "system", contextId = "systemClientForSales", url = "http://localhost:8082")
public interface SystemFeignClient {

    /**
     * 根据部门ID查询部门详情
     * sales 在创建业绩记录时需要 deptId 对应的 zoneId
     */
    @GetMapping("/api/sysDepartment/{id}")
    Result<?> getDepartmentById(@PathVariable Long id);

    /**
     * 根据战区ID查询战区详情
     */
    @GetMapping("/api/sysZone/{id}")
    Result<?> getZoneById(@PathVariable Long id);

    /**
     * 根据参数键获取参数值
     * 用于sales模块定时任务读取系统配置
     */
    @GetMapping("/api/sysParam/value/{paramKey}")
    Result<String> getParamValue(@PathVariable String paramKey);
}
