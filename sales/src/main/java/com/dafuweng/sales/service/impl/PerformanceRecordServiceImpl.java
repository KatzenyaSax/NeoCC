package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.entity.ContractEntity;
import com.dafuweng.sales.entity.PerformanceRecordEntity;
import com.dafuweng.sales.service.PerformanceRecordService;
import com.dafuweng.sales.service.ContractService;
import com.dafuweng.sales.feign.FinanceFeignClient;
import com.dafuweng.sales.dao.PerformanceRecordDao;
import com.dafuweng.common.entity.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PerformanceRecordServiceImpl implements PerformanceRecordService {

    private static final Logger log = LoggerFactory.getLogger(PerformanceRecordServiceImpl.class);

    /** 默认佣金比例 10% */
    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.10");

    @Autowired
    private PerformanceRecordDao performanceRecordDao;

    @Autowired
    private ContractService contractService;

    @Autowired
    private FinanceFeignClient financeFeignClient;

    @Override
    public PerformanceRecordEntity getById(Long id) {
        return performanceRecordDao.selectById(id);
    }

    @Override
    public PageResponse<PerformanceRecordEntity> pageList(PageRequest request) {
        IPage<PerformanceRecordEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<PerformanceRecordEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(PerformanceRecordEntity::getId);
            } else {
                wrapper.orderByDesc(PerformanceRecordEntity::getId);
            }
        } else {
            wrapper.orderByDesc(PerformanceRecordEntity::getCreatedAt);
        }
        IPage<PerformanceRecordEntity> result = performanceRecordDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<PerformanceRecordEntity> listBySalesRepId(Long salesRepId) {
        LambdaQueryWrapper<PerformanceRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PerformanceRecordEntity::getSalesRepId, salesRepId);
        return performanceRecordDao.selectList(wrapper);
    }

    @Override
    @Transactional
    public PerformanceRecordEntity save(PerformanceRecordEntity entity) {
        performanceRecordDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public PerformanceRecordEntity update(PerformanceRecordEntity entity) {
        performanceRecordDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        performanceRecordDao.deleteById(id);
    }

    @Override
    public PerformanceRecordEntity getByContractId(Long contractId) {
        LambdaQueryWrapper<PerformanceRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PerformanceRecordEntity::getContractId, contractId);
        wrapper.eq(PerformanceRecordEntity::getDeleted, 0);
        return performanceRecordDao.selectOne(wrapper);
    }

    @Override
    public PerformanceRecordEntity generateFromContract(Long contractId) {
        log.info("自动生成业绩记录开始，contractId={}", contractId);

        // 1. 检查是否已存在该合同的业绩记录（幂等性检查）
        PerformanceRecordEntity existing = getByContractId(contractId);
        if (existing != null) {
            log.warn("合同[{}]已存在业绩记录[id={}]，跳过创建", contractId, existing.getId());
            return null;
        }

        // 2. 查询合同详情
        ContractEntity contract = contractService.getById(contractId);
        if (contract == null) {
            log.error("合同[{}]不存在，无法生成业绩记录", contractId);
            return null;
        }

        // 3. 获取产品佣金比例
        BigDecimal commissionRate = getProductCommissionRate(contract.getProductId());
        log.info("合同[{}]对应产品[{}]佣金比例为: {}", contractId, contract.getProductId(), commissionRate);

        // 4. 计算佣金金额 = 合同金额 × 佣金比例
        BigDecimal contractAmount = contract.getContractAmount();
        BigDecimal commissionAmount = BigDecimal.ZERO;
        if (contractAmount != null && commissionRate != null) {
            commissionAmount = contractAmount.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        }

        // 5. 创建业绩记录
        PerformanceRecordEntity record = new PerformanceRecordEntity();
        record.setContractId(contractId);
        record.setSalesRepId(contract.getSalesRepId());
        record.setDeptId(contract.getDeptId());
        record.setZoneId(contract.getZoneId());
        record.setContractAmount(contractAmount);
        record.setCommissionRate(commissionRate);
        record.setCommissionAmount(commissionAmount);
        record.setStatus((short) 1); // 1=待确认
        record.setCalculateTime(new Date());

        // 6. 保存
        performanceRecordDao.insert(record);
        log.info("业绩记录创建成功[id={}]，contractId={}，佣金金额={}", record.getId(), contractId, commissionAmount);

        return record;
    }

    @Override
    @Transactional
    public PerformanceRecordEntity confirm(Long id) {
        PerformanceRecordEntity record = performanceRecordDao.selectById(id);
        if (record == null) {
            throw new RuntimeException("业绩记录不存在，id=" + id);
        }
        if (record.getStatus() == 2) {
            log.warn("业绩记录[id={}]已经是已确认状态，无需重复确认", id);
            return record;
        }
        record.setStatus((short) 2); // 2=已确认
        record.setConfirmTime(new Date());
        performanceRecordDao.updateById(record);
        log.info("业绩记录确认成功[id={}]，status=2，confirmTime={}", id, record.getConfirmTime());
        return record;
    }

    /**
     * 获取产品佣金比例
     * @param productId 产品ID
     * @return 佣金比例（默认10%）
     */
    private BigDecimal getProductCommissionRate(Long productId) {
        if (productId == null) {
            return DEFAULT_COMMISSION_RATE;
        }
        try {
            Result<?> result = financeFeignClient.getProductById(productId);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> product = (Map<String, Object>) result.getData();
                Object rate = product.get("commissionRate");
                if (rate != null) {
                    return new BigDecimal(rate.toString());
                }
            }
        } catch (Exception e) {
            log.warn("获取产品[{}]佣金比例失败，使用默认值: {}", productId, e.getMessage());
        }
        return DEFAULT_COMMISSION_RATE;
    }
}