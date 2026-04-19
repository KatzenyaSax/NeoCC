package com.dafuweng.sales.service;

import java.util.Map;

public interface CustomerViewService {

    /**
     * 获取客户详情（聚合所有相关数据）
     *
     * @param id 客户ID
     * @return 包含 customer、contactRecords、contracts、performanceRecords、transferLogs 的 Map
     */
    Map<String, Object> getCustomerView(Long id);
}
