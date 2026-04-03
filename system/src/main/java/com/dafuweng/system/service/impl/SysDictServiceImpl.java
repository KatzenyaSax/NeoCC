package com.dafuweng.system.service.impl;

import com.dafuweng.system.entity.SysDictEntity;
import com.dafuweng.system.service.SysDictService;
import com.dafuweng.system.dao.SysDictDao;
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
public class SysDictServiceImpl implements SysDictService {

    @Autowired
    private SysDictDao sysDictDao;

    @Override
    public SysDictEntity getById(Long id) {
        return sysDictDao.selectById(id);
    }

    @Override
    public PageResponse<SysDictEntity> pageList(PageRequest request) {
        IPage<SysDictEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysDictEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(SysDictEntity::getId);
            } else {
                wrapper.orderByDesc(SysDictEntity::getId);
            }
        } else {
            wrapper.orderByDesc(SysDictEntity::getCreatedAt);
        }
        IPage<SysDictEntity> result = sysDictDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<SysDictEntity> listByDictType(String dictType) {
        return sysDictDao.selectByDictType(dictType);
    }

    @Override
    @Transactional
    public SysDictEntity save(SysDictEntity entity) {
        sysDictDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public SysDictEntity update(SysDictEntity entity) {
        sysDictDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sysDictDao.deleteById(id);
    }
}
