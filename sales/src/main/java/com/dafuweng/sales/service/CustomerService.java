package com.dafuweng.sales.service;

import com.dafuweng.sales.entity.CustomerEntity;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface CustomerService {

    CustomerEntity getById(Long id);

    PageResponse<CustomerEntity> pageList(PageRequest request, String filterRole, Long userId, Long deptId, Long zoneId);

    List<CustomerEntity> listBySalesRepId(Long salesRepId);

    List<CustomerEntity> listByStatus(Short status);

    List<CustomerEntity> listCustomerToPublicSea(Integer publicSeaDays);

    @Transactional
    CustomerEntity save(CustomerEntity entity);

    @Transactional
    CustomerEntity update(CustomerEntity entity);

    @Transactional
    void delete(Long id);

    /**
     * 获取客户总数
     */
    Long count();

    /**
     * 获取销售代表列表（带条件过滤）
     */
    List<Map<String, Object>> listSalesReps(Long zoneId, Long deptId, Long salesRepId);

    Map<Long, String> getCustomerNamesByIds(List<Long> ids);

    Long getMinUnusedId();
}