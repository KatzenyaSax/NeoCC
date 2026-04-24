package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.entity.CustomerEntity;
import com.dafuweng.sales.service.CustomerService;
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

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerDao customerDao;

    @Override
    public CustomerEntity getById(Long id) {
        return customerDao.selectById(id);
    }

    @Override
    public PageResponse<CustomerEntity> pageList(PageRequest request, String filterRole, Long userId, Long deptId, Long zoneId) {
        IPage<CustomerEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<CustomerEntity> wrapper = new LambdaQueryWrapper<>();

        // 排除公海客户(status=5)
        wrapper.ne(CustomerEntity::getStatus, (short) 5);

        // 根据filterRole过滤
        if ("ROLE_SUPER_ADMIN".equals(filterRole)) {
            // 超级管理员：不过滤，返回全部（已排除公海）
        } else if ("ROLE_ZONE_DIRECTOR".equals(filterRole)) {
            // 战区总监：只显示本战区的客户
            if (zoneId != null) {
                wrapper.eq(CustomerEntity::getZoneId, zoneId);
            }
        } else if ("ROLE_DEPT_MANAGER".equals(filterRole)) {
            // 部门经理：只显示本部门的客户
            if (deptId != null) {
                wrapper.eq(CustomerEntity::getDeptId, deptId);
            }
        } else if ("ROLE_SALES_REP".equals(filterRole)) {
            // 销售代表：只显示自己的客户
            if (userId != null) {
                wrapper.eq(CustomerEntity::getSalesRepId, userId);
            }
        }

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
    public Long count() {
        return customerDao.selectCount(null);
    }
}