package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.entity.CustomerEntity;
import com.dafuweng.sales.entity.CustomerTransferLogEntity;
import com.dafuweng.sales.service.CustomerService;
import com.dafuweng.sales.dao.CustomerDao;
import com.dafuweng.sales.dao.CustomerTransferLogDao;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private CustomerTransferLogDao customerTransferLogDao;

    @Override
    public CustomerEntity getById(Long id) {
        return customerDao.selectById(id);
    }

    @Override
    public PageResponse<CustomerEntity> pageList(PageRequest request) {
        IPage<CustomerEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<CustomerEntity> wrapper = new LambdaQueryWrapper<>();
        
        // 名称模糊搜索
        if (StringUtils.hasText(request.getName())) {
            wrapper.like(CustomerEntity::getName, request.getName());
        }
        
        // 状态筛选
        if (request.getStatus() != null) {
            wrapper.eq(CustomerEntity::getStatus, request.getStatus());
        }
        
        // 客户类型筛选
        if (request.getCustomerType() != null) {
            wrapper.eq(CustomerEntity::getCustomerType, request.getCustomerType());
        }
        
        // 意向等级筛选
        if (request.getIntentionLevel() != null) {
            wrapper.eq(CustomerEntity::getIntentionLevel, request.getIntentionLevel());
        }
        
        // 排序
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(CustomerEntity::getId);
            } else {
                wrapper.orderByDesc(CustomerEntity::getId);
            }
        } else {
            wrapper.orderByDesc(CustomerEntity::getCreatedAt);
        }
        
        IPage<CustomerEntity> result = customerDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public List<CustomerEntity> listBySalesRepId(Long salesRepId) {
        return customerDao.selectBySalesRepId(salesRepId);
    }

    @Override
    public List<CustomerEntity> listByStatus(Short status) {
        return customerDao.selectByStatus(status);
    }

    @Override
    public List<CustomerEntity> listCustomerToPublicSea(Integer publicSeaDays) {
        return customerDao.selectCustomerToPublicSea(publicSeaDays);
    }

    @Override
    @Transactional
    public CustomerEntity save(CustomerEntity entity) {
        customerDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public CustomerEntity update(CustomerEntity entity) {
        customerDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        customerDao.deleteById(id);
    }

    @Override
    @Transactional
    public void transferToAnotherRep(Long customerId, Long toRepId, String reason, Long operatorId) {
        // 1. 获取客户当前的销售代表
        CustomerEntity customer = customerDao.selectById(customerId);
        if (customer == null) {
            throw new RuntimeException("客户不存在");
        }
        Long fromRepId = customer.getSalesRepId();

        // 2. 更新客户归属
        customer.setSalesRepId(toRepId);
        // 如果是公海客户转移到有效客户，需要修改状态
        if (customer.getStatus() != null && customer.getStatus() == 5) {
            customer.setStatus((short) 1); // 转为有效状态
        }
        customerDao.updateById(customer);

        // 3. 记录转移日志
        CustomerTransferLogEntity log = new CustomerTransferLogEntity();
        log.setCustomerId(customerId);
        log.setFromRepId(fromRepId);
        log.setToRepId(toRepId);
        log.setReason(reason);
        log.setOperateType("transfer");
        log.setOperatedBy(operatorId);
        log.setOperatedAt(new Date());
        customerTransferLogDao.insert(log);
    }
}