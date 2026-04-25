package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.entity.WorkLogEntity;
import com.dafuweng.sales.service.WorkLogService;
import com.dafuweng.sales.dao.WorkLogDao;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.sales.feign.AuthFeignClient;
import com.dafuweng.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkLogServiceImpl implements WorkLogService {

    @Autowired
    private WorkLogDao workLogDao;

    @Autowired
    private AuthFeignClient authFeignClient;

    @Override
    public WorkLogEntity getById(Long id) {
        return workLogDao.selectById(id);
    }

    @Override
    public PageResponse<WorkLogEntity> pageList(PageRequest request) {
        // 根据 PageRequest 中的参数获取销售代表 ID 列表
        List<Long> salesRepList = getSalesRepListByRequest(request);

        IPage<WorkLogEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<WorkLogEntity> wrapper = new LambdaQueryWrapper<>();

        // 仅查询符合条件的销售代表的工作日志
        if (salesRepList != null && !salesRepList.isEmpty()) {
            wrapper.in(WorkLogEntity::getSalesRepId, salesRepList);
        }

        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(WorkLogEntity::getId);
            } else {
                wrapper.orderByDesc(WorkLogEntity::getId);
            }
        } else {
            wrapper.orderByDesc(WorkLogEntity::getCreatedAt);
        }

        IPage<WorkLogEntity> result = workLogDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    /**
     * 根据 PageRequest 中的参数获取符合条件的销售代表 ID 列表
     */
    private List<Long> getSalesRepListByRequest(PageRequest request) {
        if (request.getZoneId() != null) {
            // 战区总监：查询该战区下的所有销售代表
            Result<List<Long>> result = authFeignClient.listUserIdsByZoneId(request.getZoneId());
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return result.getData();
            }
            return List.of();
        } else if (request.getDeptId() != null) {
            // 部门经理：查询该部门下的所有销售代表
            Result<List<Long>> result = authFeignClient.listUserIdsByDeptId(request.getDeptId());
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return result.getData();
            }
            return List.of();
        } else if (request.getSalesRepId() != null) {
            // 销售代表：仅查询自己的
            return List.of(request.getSalesRepId());
        } else {
            // 无过滤条件：返回所有销售代表
            return null;
        }
    }

    @Override
    public List<WorkLogEntity> listBySalesRepId(Long salesRepId) {
        LambdaQueryWrapper<WorkLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkLogEntity::getSalesRepId, salesRepId);
        return workLogDao.selectList(wrapper);
    }

    @Override
    public List<WorkLogEntity> listBySalesRepIds(List<Long> salesRepIds) {
        if (salesRepIds == null || salesRepIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        LambdaQueryWrapper<WorkLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WorkLogEntity::getSalesRepId, salesRepIds);
        wrapper.orderByDesc(WorkLogEntity::getLogDate);
        return workLogDao.selectList(wrapper);
    }

    @Override
    public boolean isDuplicate(Long salesRepId, String logDate) {
        return workLogDao.selectBySalesRepIdAndLogDate(salesRepId, logDate) != null;
    }

    @Override
    @Transactional
    public WorkLogEntity save(WorkLogEntity entity) {
        workLogDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public WorkLogEntity update(WorkLogEntity entity) {
        workLogDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        workLogDao.deleteById(id);
    }
}