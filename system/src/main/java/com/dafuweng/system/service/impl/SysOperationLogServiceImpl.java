package com.dafuweng.system.service.impl;

import com.dafuweng.system.entity.SysOperationLogEntity;
import com.dafuweng.system.service.SysOperationLogService;
import com.dafuweng.system.dao.SysOperationLogDao;
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
public class SysOperationLogServiceImpl implements SysOperationLogService {

    @Autowired
    private SysOperationLogDao sysOperationLogDao;

    @Override
    public SysOperationLogEntity getById(Long id) {
        return sysOperationLogDao.selectById(id);
    }

    @Override
    public PageResponse<SysOperationLogEntity> pageList(PageRequest request) {
        IPage<SysOperationLogEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysOperationLogEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(SysOperationLogEntity::getId);
            } else {
                wrapper.orderByDesc(SysOperationLogEntity::getId);
            }
        } else {
            wrapper.orderByDesc(SysOperationLogEntity::getCreatedAt);
        }
        IPage<SysOperationLogEntity> result = sysOperationLogDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<SysOperationLogEntity> listByUserId(Long userId) {
        LambdaQueryWrapper<SysOperationLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOperationLogEntity::getUserId, userId);
        wrapper.orderByDesc(SysOperationLogEntity::getCreatedAt);
        return sysOperationLogDao.selectList(wrapper);
    }

    @Override
    public List<SysOperationLogEntity> listByModule(String module) {
        LambdaQueryWrapper<SysOperationLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOperationLogEntity::getModule, module);
        wrapper.orderByDesc(SysOperationLogEntity::getCreatedAt);
        return sysOperationLogDao.selectList(wrapper);
    }

    @Override
    @Transactional
    public SysOperationLogEntity save(SysOperationLogEntity entity) {
        sysOperationLogDao.insert(entity);
        return entity;
    }
}
