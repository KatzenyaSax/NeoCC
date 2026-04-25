package com.dafuweng.finance.service.impl;

import com.dafuweng.finance.entity.LoanAuditEntity;
import com.dafuweng.finance.entity.LoanAuditRecordEntity;
import com.dafuweng.finance.entity.FinanceProductEntity;
import com.dafuweng.finance.service.LoanAuditService;
import com.dafuweng.finance.service.LoanAuditRecordService;
import com.dafuweng.finance.service.FinanceProductService;
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
    private LoanAuditRecordService loanAuditRecordService;

    @Autowired
    private FinanceProductService financeProductService;

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
        wrapper.eq(LoanAuditEntity::getDeleted, (short) 0);
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
        wrapper.eq(LoanAuditEntity::getDeleted, (short) 0);
        return loanAuditDao.selectList(wrapper);
    }

    @Override
    @Transactional
    public LoanAuditEntity save(LoanAuditEntity entity) {
        entity.setDeleted((short) 0);
        loanAuditDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public LoanAuditEntity update(LoanAuditEntity entity) {
        if (entity.getDeleted() != null && entity.getDeleted() == 1) {
            loanAuditDao.softDeleteById(entity.getId());
        } else {
            loanAuditDao.updateById(entity);
        }
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        loanAuditDao.deleteById(id);
    }

    private void saveRecord(Long auditId, String action, Long operatorId,
                            String operatorName, String operatorRole, String comment) {
        LoanAuditRecordEntity record = new LoanAuditRecordEntity();
        record.setLoanAuditId(auditId);
        record.setAction(action);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setOperatorRole(operatorRole);
        record.setContent(comment);
        record.setCreatedAt(new Date());
        loanAuditRecordService.save(record);
    }

    @Override
    @Transactional
    public void receive(Long id, Long operatorId, String operatorName, String operatorRole, String comment) {
        LoanAuditEntity audit = loanAuditDao.selectById(id);
        if (audit == null) {
            throw new IllegalArgumentException("审核记录不存在");
        }
        if (audit.getAuditStatus() != 1) {
            throw new IllegalStateException("当前状态不允许接收合同，当前状态：" + audit.getAuditStatus());
        }
        audit.setAuditStatus((short) 2);
        loanAuditDao.updateById(audit);
        saveRecord(id, "receive", operatorId, operatorName, operatorRole, comment);
    }

    @Override
    @Transactional
    public void review(Long id, Long operatorId, String operatorName, String operatorRole, String comment) {
        LoanAuditEntity audit = loanAuditDao.selectById(id);
        if (audit == null) {
            throw new IllegalArgumentException("审核记录不存在");
        }
        if (audit.getAuditStatus() != 2) {
            throw new IllegalStateException("当前状态不允许初审，当前状态：" + audit.getAuditStatus());
        }
        audit.setAuditStatus((short) 3);
        audit.setAuditDate(new Date());
        loanAuditDao.updateById(audit);
        saveRecord(id, "review", operatorId, operatorName, operatorRole, comment);
    }

    @Override
    @Transactional
    public void submitBank(Long id, Long bankId, Long operatorId,
                           String operatorName, String operatorRole, String comment) {
        LoanAuditEntity audit = loanAuditDao.selectById(id);
        if (audit == null) {
            throw new IllegalArgumentException("审核记录不存在");
        }
        if (audit.getAuditStatus() != 3) {
            throw new IllegalStateException("当前状态不允许提交银行，当前状态：" + audit.getAuditStatus());
        }
        audit.setAuditStatus((short) 4);
        audit.setBankId(bankId);
        audit.setBankApplyTime(new Date());
        loanAuditDao.updateById(audit);
        saveRecord(id, "submit_bank", operatorId, operatorName, operatorRole, comment);
    }

    @Override
    @Transactional
    public void bankResult(Long id, boolean approved, String bankFeedbackContent,
                           Long operatorId, String operatorName, String operatorRole, String comment) {
        LoanAuditEntity audit = loanAuditDao.selectById(id);
        if (audit == null) {
            throw new IllegalArgumentException("审核记录不存在");
        }
        if (audit.getAuditStatus() != 4) {
            throw new IllegalStateException("当前状态不允许银行反馈，当前状态：" + audit.getAuditStatus());
        }
        audit.setBankFeedbackTime(new Date());
        audit.setBankFeedbackContent(bankFeedbackContent);
        if (approved) {
            audit.setAuditStatus((short) 6);
            saveRecord(id, "bank_result", operatorId, operatorName, operatorRole, "银行通过：" + bankFeedbackContent);
        } else {
            audit.setAuditStatus((short) 5);
            saveRecord(id, "bank_result", operatorId, operatorName, operatorRole, "银行拒绝：" + bankFeedbackContent);
        }
        loanAuditDao.updateById(audit);
    }

    @Override
    @Transactional
    public void approve(Long id, Long operatorId, String operatorName, String operatorRole, String comment,
                        BigDecimal actualLoanAmount, BigDecimal actualInterestRate, Date loanGrantedDate) {
        LoanAuditEntity audit = loanAuditDao.selectById(id);
        if (audit == null) {
            throw new IllegalArgumentException("审核记录不存在");
        }
        if (audit.getAuditStatus() != 6) {
            throw new IllegalStateException("当前状态不允许终审，当前状态：" + audit.getAuditStatus());
        }
        // audit_status=6 已经是终审通过，这里仅记录终审意见和放款信息
        audit.setActualLoanAmount(actualLoanAmount);
        audit.setActualInterestRate(actualInterestRate);
        audit.setLoanGrantedDate(loanGrantedDate);
        audit.setAuditOpinion(comment);
        loanAuditDao.updateById(audit);

        saveRecord(id, "approve", operatorId, operatorName, operatorRole, comment);

        // 触发 OpenFeign 通知 sales 创建业绩
        Result<ContractVO> contractResult = salesFeignClient.getContract(audit.getContractId());
        if (contractResult == null || contractResult.getData() == null) {
            throw new RuntimeException("无法获取合同信息，无法创建业绩");
        }
        ContractVO contract = contractResult.getData();

        PerformanceCreateDTO perfDto = new PerformanceCreateDTO();
        perfDto.setContractId(audit.getContractId());
        perfDto.setCustomerId(contract.getCustomerId());
        perfDto.setSalesRepId(contract.getSalesRepId());
        perfDto.setDeptId(contract.getDeptId());
        perfDto.setZoneId(contract.getZoneId());
        perfDto.setContractAmount(contract.getContractAmount());

        // 查询金融产品获取提成比例
        if (contract.getProductId() != null) {
            FinanceProductEntity product = financeProductService.getById(contract.getProductId());
            if (product != null && product.getCommissionRate() != null) {
                perfDto.setCommissionRate(product.getCommissionRate());
                perfDto.setCommissionAmount(contract.getContractAmount().multiply(product.getCommissionRate()));
            } else {
                perfDto.setCommissionRate(BigDecimal.ZERO);
                perfDto.setCommissionAmount(BigDecimal.ZERO);
            }
        } else {
            perfDto.setCommissionRate(BigDecimal.ZERO);
            perfDto.setCommissionAmount(BigDecimal.ZERO);
        }
        perfDto.setStatus((short) 0);
        perfDto.setCalculateTime(new Date());

        Result<?> perfResult = salesFeignClient.createPerformance(perfDto);
        if (perfResult == null || perfResult.getCode() != 200) {
            throw new RuntimeException("创建业绩记录失败: " + (perfResult != null ? perfResult.getMessage() : "未知错误"));
        }

        // 更新合同状态 → 已放款（status=7）
        salesFeignClient.updateContractStatus(audit.getContractId(), (short) 7);
    }

    @Override
    @Transactional
    public void reject(Long id, Long operatorId, String operatorName, String operatorRole, String comment) {
        LoanAuditEntity audit = loanAuditDao.selectById(id);
        if (audit == null) {
            throw new IllegalArgumentException("审核记录不存在");
        }
        // 终审拒绝：可以是 4(银行通过) / 5(银行拒绝) / 7(终审拒绝后重新申请)
        if (audit.getAuditStatus() != 4 && audit.getAuditStatus() != 5 && audit.getAuditStatus() != 7) {
            throw new IllegalStateException("当前状态不允许终审拒绝，当前状态：" + audit.getAuditStatus());
        }
        audit.setAuditStatus((short) 7);
        loanAuditDao.updateById(audit);
        saveRecord(id, "reject", operatorId, operatorName, operatorRole, comment);
    }

    @Override
    public Long getMinUnusedId() {
        return loanAuditDao.selectMinUnusedId();
    }
}
