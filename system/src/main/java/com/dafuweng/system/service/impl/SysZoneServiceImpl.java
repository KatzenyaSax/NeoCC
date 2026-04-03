package com.dafuweng.system.service.impl;

import com.dafuweng.system.entity.SysZoneEntity;
import com.dafuweng.system.service.SysZoneService;
import com.dafuweng.system.dao.SysZoneDao;
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
public class SysZoneServiceImpl implements SysZoneService {

    @Autowired
    private SysZoneDao sysZoneDao;

    @Override
    public SysZoneEntity getById(Long id) {
        return sysZoneDao.selectById(id);
    }

    @Override
    public PageResponse<SysZoneEntity> pageList(PageRequest request) {
        IPage<SysZoneEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysZoneEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(SysZoneEntity::getId);
            } else {
                wrapper.orderByDesc(SysZoneEntity::getId);
            }
        } else {
            wrapper.orderByDesc(SysZoneEntity::getCreatedAt);
        }
        IPage<SysZoneEntity> result = sysZoneDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<SysZoneEntity> listAll() {
        LambdaQueryWrapper<SysZoneEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysZoneEntity::getSortOrder);
        return sysZoneDao.selectList(wrapper);
    }

    @Override
    public List<SysZoneEntity> listByStatus(Short status) {
        LambdaQueryWrapper<SysZoneEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysZoneEntity::getStatus, status);
        wrapper.orderByAsc(SysZoneEntity::getSortOrder);
        return sysZoneDao.selectList(wrapper);
    }

    @Override
    @Transactional
    public SysZoneEntity save(SysZoneEntity entity) {
        sysZoneDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public SysZoneEntity update(SysZoneEntity entity) {
        sysZoneDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sysZoneDao.deleteById(id);
    }
}
