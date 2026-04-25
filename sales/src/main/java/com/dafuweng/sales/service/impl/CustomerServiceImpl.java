package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.entity.CustomerEntity;
import com.dafuweng.sales.service.CustomerService;
import com.dafuweng.sales.dao.CustomerDao;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.common.entity.Result;
import com.dafuweng.sales.feign.AuthFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private AuthFeignClient authFeignClient;

    @Override
    public List<Map<String, Object>> listSalesReps(Long zoneId, Long deptId, Long salesRepId) {
        // 使用auth模块的新接口
        // 由于auth模块的接口没有直接暴露带参数的sales-reps接口，我们需要自己实现过滤
        Result<?> res = authFeignClient.listSalesReps();
        if (res != null && res.getCode() == 200 && res.getData() != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allSalesReps = (List<Map<String, Object>>) res.getData();

            // 根据条件过滤
            List<Map<String, Object>> filteredReps = allSalesReps.stream()
                    .filter(rep -> {
                        // 如果有zoneId条件，只保留匹配的用户
                        if (zoneId != null) {
                            Object repZoneId = rep.get("zoneId");
                            return repZoneId != null && Objects.equals(Long.parseLong(String.valueOf(repZoneId)), zoneId);
                        }
                        // 如果有deptId条件，只保留匹配的用户
                        if (deptId != null) {
                            Object repDeptId = rep.get("deptId");
                            return repDeptId != null && Objects.equals(Long.parseLong(String.valueOf(repDeptId)), deptId);
                        }
                        // 如果有salesRepId条件，只保留匹配的用户
                        if (salesRepId != null) {
                            Object repId = rep.get("id");
                            return repId != null && Objects.equals(Long.parseLong(String.valueOf(repId)), salesRepId);
                        }
                        // 没有条件时返回所有
                        return true;
                    })
                    .collect(Collectors.toList());

            return filteredReps;
        }
        return new ArrayList<>();
    }

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

    @Override
    public Map<Long, String> getCustomerNamesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }
        List<CustomerEntity> customers = customerDao.selectBatchIds(ids);
        return customers.stream()
                .collect(Collectors.toMap(CustomerEntity::getId, c -> c.getName() != null ? c.getName() : ""));
    }

    @Override
    public Long getMinUnusedId() {
        return customerDao.selectMinUnusedId();
    }
}