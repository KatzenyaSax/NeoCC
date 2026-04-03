package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.entity.WorkLogEntity;
import com.dafuweng.sales.service.WorkLogService;
import com.dafuweng.sales.dao.WorkLogDao;
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
public class WorkLogServiceImpl implements WorkLogService {

    @Autowired
    private WorkLogDao workLogDao;

    @Override
    public WorkLogEntity getById(Long id) {
        return workLogDao.selectById(id);
    }

    @Override
    public PageResponse<WorkLogEntity> pageList(PageRequest request) {
        IPage<WorkLogEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<WorkLogEntity> wrapper = new LambdaQueryWrapper<>();
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

    @Override
    public List<WorkLogEntity> listBySalesRepId(Long salesRepId) {
        LambdaQueryWrapper<WorkLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkLogEntity::getSalesRepId, salesRepId);
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