package com.dafuweng.finance.service.impl;

import com.dafuweng.finance.entity.LoanAuditEntity;
import com.dafuweng.finance.service.LoanAuditService;
import com.dafuweng.finance.dao.LoanAuditDao;
import com.dafuweng.finance.feign.SalesFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.dto.PerformanceCreateDTO;
import com.dafuweng.common.entity.vo.ContractVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class LoanAuditServiceImpl implements LoanAuditService {

    @Autowired
    private LoanAuditDao loanAuditDao;

    @Autowired
    private SalesFeignClient salesFeignClient;

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

    /**
     * 终审通过
     * 触发：1. 更新 loan_audit 状态  2. 更新 contract 状态  3. 创建业绩记录
     */
    @Override
    @Transactional
    public void approve(Long loanAuditId, BigDecimal actualLoanAmount,
            BigDecimal actualInterestRate, Date loanGrantedDate) {
        LoanAuditEntity loanAudit = loanAuditDao.selectById(loanAuditId);
        if (loanAudit == null) {
            throw new IllegalArgumentException("审核记录不存在");
        }

        // 1. 更新 loan_audit 状态为终审通过
        loanAudit.setAuditStatus((short) 5);
        loanAudit.setActualLoanAmount(actualLoanAmount);
        loanAudit.setActualInterestRate(actualInterestRate);
        loanAudit.setLoanGrantedDate(loanGrantedDate);
        loanAuditDao.updateById(loanAudit);

        // 2. 更新合同状态 → 已放款（status=7）
        salesFeignClient.updateContractStatus(loanAudit.getContractId(), (short) 7);

        // 3. 查询合同信息，构建业绩记录
        Result<ContractVO> contractResult = salesFeignClient.getContract(loanAudit.getContractId());
        if (contractResult == null || contractResult.getData() == null) {
            throw new RuntimeException("无法获取合同信息，无法创建业绩");
        }
        ContractVO contract = contractResult.getData();

        PerformanceCreateDTO perfDto = new PerformanceCreateDTO();
        perfDto.setContractId(loanAudit.getContractId());
        perfDto.setCustomerId(contract.getCustomerId());
        perfDto.setSalesRepId(contract.getSalesRepId());
        perfDto.setDeptId(contract.getDeptId());
        // zoneId 需要从 system 查到或合同表有 zone_id 字段，此处传 null
        perfDto.setZoneId(null);
        perfDto.setContractAmount(contract.getContractAmount());
        // commissionRate 和 commissionAmount 需要从产品查到，此处传 0
        perfDto.setCommissionRate(BigDecimal.ZERO);
        perfDto.setCommissionAmount(BigDecimal.ZERO);
        perfDto.setStatus((short) 0);  // 待计算
        perfDto.setCalculateTime(new Date());

        Result<?> perfResult = salesFeignClient.createPerformance(perfDto);
        if (perfResult == null || perfResult.getCode() != 200) {
            throw new RuntimeException("创建业绩记录失败: " + (perfResult != null ? perfResult.getMessage() : "未知错误"));
        }
    }
}
