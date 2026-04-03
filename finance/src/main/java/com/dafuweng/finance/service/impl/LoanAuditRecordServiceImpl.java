package com.dafuweng.finance.service.impl;

import com.dafuweng.finance.entity.LoanAuditRecordEntity;
import com.dafuweng.finance.service.LoanAuditRecordService;
import com.dafuweng.finance.dao.LoanAuditRecordDao;
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
public class LoanAuditRecordServiceImpl implements LoanAuditRecordService {

    @Autowired
    private LoanAuditRecordDao loanAuditRecordDao;

    @Override
    public LoanAuditRecordEntity getById(Long id) {
        return loanAuditRecordDao.selectById(id);
    }

    @Override
    public PageResponse<LoanAuditRecordEntity> pageList(PageRequest request) {
        IPage<LoanAuditRecordEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<LoanAuditRecordEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(LoanAuditRecordEntity::getId);
            } else {
                wrapper.orderByDesc(LoanAuditRecordEntity::getId);
            }
        } else {
            wrapper.orderByDesc(LoanAuditRecordEntity::getCreatedAt);
        }
        IPage<LoanAuditRecordEntity> result = loanAuditRecordDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<LoanAuditRecordEntity> listByLoanAuditId(Long loanAuditId) {
        return loanAuditRecordDao.selectByLoanAuditId(loanAuditId);
    }

    @Override
    @Transactional
    public LoanAuditRecordEntity save(LoanAuditRecordEntity entity) {
        loanAuditRecordDao.insert(entity);
        return entity;
    }
}
