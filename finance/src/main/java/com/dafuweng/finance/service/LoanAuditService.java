package com.dafuweng.finance.service;

import com.dafuweng.finance.entity.LoanAuditEntity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface LoanAuditService {

    LoanAuditEntity getById(Long id);

    LoanAuditEntity getByContractId(Long contractId);

    PageResponse<LoanAuditEntity> pageList(PageRequest request);

    List<LoanAuditEntity> listByFinanceSpecialistId(Long financeSpecialistId);

    @Transactional
    LoanAuditEntity save(LoanAuditEntity entity);

    @Transactional
    LoanAuditEntity update(LoanAuditEntity entity);

    @Transactional
    void delete(Long id);

    @Transactional
    void approve(Long loanAuditId, BigDecimal actualLoanAmount, BigDecimal actualInterestRate, Date loanGrantedDate);
}
