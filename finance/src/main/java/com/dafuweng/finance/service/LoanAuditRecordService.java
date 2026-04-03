package com.dafuweng.finance.service;

import com.dafuweng.finance.entity.LoanAuditRecordEntity;
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

public interface LoanAuditRecordService {

    LoanAuditRecordEntity getById(Long id);

    PageResponse<LoanAuditRecordEntity> pageList(PageRequest request);

    List<LoanAuditRecordEntity> listByLoanAuditId(Long loanAuditId);

    @Transactional
    LoanAuditRecordEntity save(LoanAuditRecordEntity entity);
}
