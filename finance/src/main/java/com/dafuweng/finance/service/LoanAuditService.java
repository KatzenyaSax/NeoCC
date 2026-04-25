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

    /** 接收合同（1→2） */
    @Transactional
    void receive(Long id, Long operatorId, String operatorName, String operatorRole, String comment);

    /** 初审（2→3） */
    @Transactional
    void review(Long id, Long operatorId, String operatorName, String operatorRole, String comment);

    /** 提交银行（3→4） */
    @Transactional
    void submitBank(Long id, Long bankId, Long operatorId, String operatorName, String operatorRole, String comment);

    /** 银行反馈（4→6 approve / 4→7 reject） */
    @Transactional
    void bankResult(Long id, boolean approved, String bankFeedbackContent,
                    Long operatorId, String operatorName, String operatorRole, String comment);

    /** 终审通过（6→终态，触发业绩创建） */
    @Transactional
    void approve(Long id, Long operatorId, String operatorName, String operatorRole, String comment,
                 BigDecimal actualLoanAmount, BigDecimal actualInterestRate, Date loanGrantedDate);

    /** 终审拒绝（4/5/7→终态） */
    @Transactional
    void reject(Long id, Long operatorId, String operatorName, String operatorRole, String comment);

    Long getMinUnusedId();
}
