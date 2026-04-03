package com.dafuweng.system.service.impl;

import com.dafuweng.system.entity.SysParamEntity;
import com.dafuweng.system.service.SysParamService;
import com.dafuweng.system.dao.SysParamDao;
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
public class SysParamServiceImpl implements SysParamService {

    @Autowired
    private SysParamDao sysParamDao;

    @Override
    public SysParamEntity getById(Long id) {
        return sysParamDao.selectById(id);
    }

    @Override
    public SysParamEntity getByParamKey(String paramKey) {
        return sysParamDao.selectByParamKey(paramKey);
    }

    @Override
    public PageResponse<SysParamEntity> pageList(PageRequest request) {
        IPage<SysParamEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysParamEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(SysParamEntity::getId);
            } else {
                wrapper.orderByDesc(SysParamEntity::getId);
            }
        } else {
            wrapper.orderByDesc(SysParamEntity::getCreatedAt);
        }
        IPage<SysParamEntity> result = sysParamDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<SysParamEntity> listByParamGroup(String paramGroup) {
        LambdaQueryWrapper<SysParamEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysParamEntity::getParamGroup, paramGroup);
        wrapper.orderByAsc(SysParamEntity::getSortOrder);
        return sysParamDao.selectList(wrapper);
    }

    @Override
    @Transactional
    public SysParamEntity save(SysParamEntity entity) {
        sysParamDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public SysParamEntity update(SysParamEntity entity) {
        sysParamDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sysParamDao.deleteById(id);
    }
}
