package com.dafuweng.finance.service.impl;

import com.dafuweng.finance.entity.BankEntity;
import com.dafuweng.finance.service.BankService;
import com.dafuweng.finance.dao.BankDao;
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
public class BankServiceImpl implements BankService {

    @Autowired
    private BankDao bankDao;

    @Override
    public BankEntity getById(Long id) {
        return bankDao.selectById(id);
    }

    @Override
    public PageResponse<BankEntity> pageList(PageRequest request) {
        IPage<BankEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<BankEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(BankEntity::getId);
            } else {
                wrapper.orderByDesc(BankEntity::getId);
            }
        } else {
            wrapper.orderByDesc(BankEntity::getCreatedAt);
        }
        IPage<BankEntity> result = bankDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<BankEntity> listByStatus(Short status) {
        LambdaQueryWrapper<BankEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BankEntity::getStatus, status);
        return bankDao.selectList(wrapper);
    }

    @Override
    @Transactional
    public BankEntity save(BankEntity entity) {
        bankDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public BankEntity update(BankEntity entity) {
        bankDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        bankDao.deleteById(id);
    }

    @Override
    public Long getMinUnusedId() {
        return bankDao.selectMinUnusedId();
    }
}
