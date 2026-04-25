package com.dafuweng.sales.service;

import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.sales.entity.CustomerEntity;

import java.util.List;
import java.util.Map;

public interface PublicSeaService {

    /**
     * 公海客户分页列表
     */
    PageResponse<CustomerEntity> pageList(PageRequest request);

    /**
     * 转移公海客户
     *
     * @param customerId 客户ID
     * @param toRepId    转入销售代表ID
     * @param reason     转移原因
     * @param operatorId 操作人ID
     */
    void transfer(Long customerId, Long toRepId, String reason, Long operatorId);

    /**
     * 获取销售代表列表（下拉用）
     */
    List<Map<String, Object>> listSalesReps();

    /**
     * 获取销售代表列表（带条件过滤）
     */
    List<Map<String, Object>> listSalesReps(Long zoneId, Long deptId, Long salesRepId);
}
