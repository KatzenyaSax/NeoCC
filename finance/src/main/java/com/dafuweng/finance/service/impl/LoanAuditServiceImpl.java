package com.dafuweng.finance.service.impl;

import com.dafuweng.finance.entity.LoanAuditEntity;
import com.dafuweng.finance.service.LoanAuditService;
import com.dafuweng.finance.dao.LoanAuditDao;
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
public class LoanAuditServiceImpl implements LoanAuditService {

    @Autowired
    private LoanAuditDao loanAuditDao;

    @Override
    public LoanAuditEntity getById(Long id) {
        return loanAuditDao.selectById(id);
    }

    @Override
    public LoanAuditEntity getByContractId(Long contractId) {
        return loanAuditDao.selectByContractId(contractId);
    }

    @Override
    public PageResponse<LoanAuditEntity> pageList(PageRequest request) {
        IPage<LoanAuditEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<LoanAuditEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(LoanAuditEntity::getId);
            } else {
                wrapper.orderByDesc(LoanAuditEntity::getId);
            }
        } else {
            wrapper.orderByDesc(LoanAuditEntity::getCreatedAt);
        }
        IPage<LoanAuditEntity> result = loanAuditDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<LoanAuditEntity> listByFinanceSpecialistId(Long financeSpecialistId) {
        LambdaQueryWrapper<LoanAuditEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoanAuditEntity::getFinanceSpecialistId, financeSpecialistId);
        return loanAuditDao.selectList(wrapper);
    }

    @Override
    @Transactional
    public LoanAuditEntity save(LoanAuditEntity entity) {
        loanAuditDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public LoanAuditEntity update(LoanAuditEntity entity) {
        loanAuditDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        loanAuditDao.deleteById(id);
    }
}
