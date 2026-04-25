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
import java.util.stream.Collectors;

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
    public void transfer(Long customerId, Long toRepId, String reason, Long operatorId, String operateType) {
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
        // 3. 获取目标销售代表的部门ID和战区ID
        Result<?> repRes = authFeignClient.getUserById(toRepId);
        Long deptId = null;
        Long zoneId = null;
        if (repRes != null && repRes.getCode() == 200 && repRes.getData() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> repUser = (Map<String, Object>) repRes.getData();
            Object repDeptId = repUser.get("deptId");
            Object repZoneId = repUser.get("zoneId");
            if (repDeptId != null) {
                deptId = Long.parseLong(String.valueOf(repDeptId));
            }
            if (repZoneId != null) {
                zoneId = Long.parseLong(String.valueOf(repZoneId));
            }
        }
        // 4. 更新客户
        customer.setSalesRepId(toRepId);
        customer.setDeptId(deptId);
        customer.setZoneId(zoneId);
        customer.setStatus((short) 1);
        customerDao.updateById(customer);
        // 5. 写入转移日志
        CustomerTransferLogEntity log = new CustomerTransferLogEntity();
        log.setId(customerTransferLogDao.selectMinUnusedId());
        log.setCustomerId(customerId);
        log.setFromRepId(null);
        log.setToRepId(toRepId);
        log.setOperateType(operateType);
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
}
