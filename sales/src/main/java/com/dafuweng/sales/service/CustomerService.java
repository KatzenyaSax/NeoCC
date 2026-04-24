package com.dafuweng.sales.service;

import com.dafuweng.sales.entity.CustomerEntity;
import com.dafuweng.sales.dao.CustomerDao;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

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
}