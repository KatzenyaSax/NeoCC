package com.dafuweng.sales.service.impl;

import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.common.entity.Result;
import com.dafuweng.sales.entity.CustomerEntity;
import com.dafuweng.sales.entity.CustomerTransferLogEntity;
import com.dafuweng.sales.dao.CustomerDao;
import com.dafuweng.sales.dao.CustomerTransferLogDao;
import com.dafuweng.sales.feign.AuthFeignClient;
import com.dafuweng.sales.service.PublicSeaService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class PublicSeaServiceImpl implements PublicSeaService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private CustomerTransferLogDao customerTransferLogDao;

    @Autowired
    private AuthFeignClient authFeignClient;

    @Override
    public PageResponse<CustomerEntity> pageList(PageRequest request) {
        IPage<CustomerEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<CustomerEntity> wrapper = new LambdaQueryWrapper<>();
        // 只查公海客户
        wrapper.eq(CustomerEntity::getStatus, (short) 5);
        // 客户名称模糊搜索
        if (StringUtils.hasText(request.getName())) {
            wrapper.like(CustomerEntity::getName, request.getName());
        }
        // 排序
        if ("asc".equalsIgnoreCase(request.getSortOrder())) {
            wrapper.orderByAsc(CustomerEntity::getId);
        } else {
            wrapper.orderByDesc(CustomerEntity::getCreatedAt);
        }
        IPage<CustomerEntity> result = customerDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    @Transactional
    public void transfer(Long customerId, Long toRepId, String reason, Long operatorId) {
        // 1. 校验客户
        CustomerEntity customer = customerDao.selectById(customerId);
        if (customer == null) {
            throw new RuntimeException("客户不存在");
        }
        if (customer.getStatus() != 5) {
            throw new RuntimeException("该客户不是公海客户");
        }
        // 2. 权限校验：销售代表只能转给自己
        Result<?> res = authFeignClient.getUserById(operatorId);
        boolean isSalesRep = false;
        if (res != null && res.getCode() == 200 && res.getData() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) res.getData();
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) user.get("roles");
            if (roles != null && roles.contains("sales_rep")) {
                isSalesRep = true;
            }
        }
        if (isSalesRep && !toRepId.equals(operatorId)) {
            throw new RuntimeException("销售代表只能将公海客户转移给自己");
        }
        // 3. 更新客户
        customer.setSalesRepId(toRepId);
        customer.setStatus((short) 1);
        customerDao.updateById(customer);
        // 4. 写入转移日志
        CustomerTransferLogEntity log = new CustomerTransferLogEntity();
        log.setCustomerId(customerId);
        log.setFromRepId(null);
        log.setToRepId(toRepId);
        log.setOperateType(toRepId.equals(operatorId) ? "claim" : "assign");
        log.setReason(reason);
        log.setOperatedBy(operatorId);
        log.setOperatedAt(new Date());
        customerTransferLogDao.insert(log);
    }

    @Override
    public List<Map<String, Object>> listSalesReps() {
        Result<?> res = authFeignClient.listSalesReps();
        if (res != null && res.getCode() == 200 && res.getData() != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) res.getData();
            return list;
        }
        return new ArrayList<>();
    }
}
