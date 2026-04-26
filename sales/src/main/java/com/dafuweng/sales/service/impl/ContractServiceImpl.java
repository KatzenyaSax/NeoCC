package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.entity.ContractEntity;
import com.dafuweng.sales.entity.vo.ContractDetailVO;
import com.dafuweng.sales.service.ContractService;
import com.dafuweng.sales.service.PerformanceRecordService;
import com.dafuweng.sales.dao.ContractDao;
import com.dafuweng.sales.feign.AuthFeignClient;
import com.dafuweng.sales.feign.FinanceFeignClient;
import com.dafuweng.sales.feign.SystemFeignClient;
import com.dafuweng.sales.entity.CustomerEntity;
import com.dafuweng.sales.entity.PerformanceRecordEntity;
import com.dafuweng.sales.dao.CustomerDao;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractDao contractDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private AuthFeignClient authFeignClient;

    @Autowired
    private SystemFeignClient systemFeignClient;

    @Autowired
    private FinanceFeignClient financeFeignClient;

    @Autowired
    private PerformanceRecordService performanceRecordService;

    @Override
    public ContractEntity getById(Long id) {
        return contractDao.selectById(id);
    }

    @Override
    public ContractEntity getByContractNo(String contractNo) {
        return contractDao.selectByContractNo(contractNo);
    }

    @Override
    public PageResponse<ContractEntity> pageList(PageRequest request, String filterRole, Long userId, Long deptId, Long zoneId, String contractNo) {
        IPage<ContractEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<ContractEntity> wrapper = new LambdaQueryWrapper<>();

        // 根据filterRole过滤
        if ("ROLE_SUPER_ADMIN".equals(filterRole)) {
            // 超级管理员：不过滤，返回全部
        } else if ("ROLE_ZONE_DIRECTOR".equals(filterRole)) {
            // 战区总监：只显示本战区的合同
            if (zoneId != null) {
                wrapper.eq(ContractEntity::getZoneId, zoneId);
            }
        } else if ("ROLE_DEPT_MANAGER".equals(filterRole)) {
            // 部门经理：只显示本部门的合同
            if (deptId != null) {
                wrapper.eq(ContractEntity::getDeptId, deptId);
            }
        } else if ("ROLE_SALES_REP".equals(filterRole)) {
            // 销售代表：只显示自己的合同
            if (userId != null) {
                wrapper.eq(ContractEntity::getSalesRepId, userId);
            }
        }

        // 按合同编号搜索
        if (StringUtils.hasText(contractNo)) {
            wrapper.like(ContractEntity::getContractNo, contractNo);
        }

        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(ContractEntity::getId);
            } else {
                wrapper.orderByDesc(ContractEntity::getId);
            }
        } else {
            wrapper.orderByDesc(ContractEntity::getCreatedAt);
        }
        wrapper.eq(ContractEntity::getDeleted, (short) 0);
        IPage<ContractEntity> result = contractDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<ContractEntity> listBySalesRepId(Long salesRepId) {
        LambdaQueryWrapper<ContractEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContractEntity::getSalesRepId, salesRepId);
        wrapper.eq(ContractEntity::getDeleted, (short) 0);
        return contractDao.selectList(wrapper);
    }

    @Override
    public List<ContractEntity> listByStatus(Short status) {
        LambdaQueryWrapper<ContractEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContractEntity::getStatus, status);
        wrapper.eq(ContractEntity::getDeleted, (short) 0);
        return contractDao.selectList(wrapper);
    }

    @Override
    public PageResponse<ContractDetailVO> pageListByStatusWithNames(int pageNum, int pageSize, Short status) {
        IPage<ContractEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ContractEntity> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(ContractEntity::getStatus, status);
        }

        wrapper.eq(ContractEntity::getDeleted, (short) 0);
        wrapper.orderByDesc(ContractEntity::getCreatedAt);
        IPage<ContractEntity> result = contractDao.selectPage(page, wrapper);

        // 转换为VO并填充名称
        List<ContractDetailVO> voList = result.getRecords().stream()
                .map(this::convertToDetailVOWithNames)
                .collect(java.util.stream.Collectors.toList());

        return PageResponse.of(result.getTotal(), voList,
            (int) result.getCurrent(), (int) result.getSize());
    }

    /**
     * 将ContractEntity转换为ContractDetailVO并填充名称
     */
    private ContractDetailVO convertToDetailVOWithNames(ContractEntity entity) {
        ContractDetailVO vo = new ContractDetailVO();
        org.springframework.beans.BeanUtils.copyProperties(entity, vo);

        // 填充客户名称
        if (entity.getCustomerId() != null) {
            CustomerEntity customer = customerDao.selectById(entity.getCustomerId());
            if (customer != null) {
                vo.setCustomerName(customer.getName());
            }
        }

        // 填充销售代表名称
        if (entity.getSalesRepId() != null) {
            Result<?> res = authFeignClient.getUserById(entity.getSalesRepId());
            if (res != null && res.getCode() == 200 && res.getData() != null) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> user = (java.util.Map<String, Object>) res.getData();
                vo.setSalesRepName((String) user.get("realName"));
            }
        }

        // 填充部门名称
        if (entity.getDeptId() != null) {
            Result<?> res = systemFeignClient.getDepartmentById(entity.getDeptId());
            if (res != null && res.getCode() == 200 && res.getData() != null) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> dept = (java.util.Map<String, Object>) res.getData();
                vo.setDeptName((String) dept.get("deptName"));
            }
        }

        // 填充战区位名称
        if (entity.getZoneId() != null) {
            Result<?> res = systemFeignClient.getZoneById(entity.getZoneId());
            if (res != null && res.getCode() == 200 && res.getData() != null) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> zone = (java.util.Map<String, Object>) res.getData();
                vo.setZoneName((String) zone.get("zoneName"));
            }
        }

        // 填充产品名称
        if (entity.getProductId() != null) {
            Result<?> res = financeFeignClient.getById(entity.getProductId());
            if (res != null && res.getCode() == 200 && res.getData() != null) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> product = (java.util.Map<String, Object>) res.getData();
                vo.setProductName((String) product.get("productName"));
            }
        }

        return vo;
    }

    @Override
    public PageResponse<ContractEntity> pageListByStatus(int pageNum, int pageSize, Short status) {
        IPage<ContractEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ContractEntity> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(ContractEntity::getStatus, status);
        }

        wrapper.eq(ContractEntity::getDeleted, (short) 0);
        wrapper.orderByDesc(ContractEntity::getCreatedAt);
        IPage<ContractEntity> result = contractDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    @Transactional
    public ContractEntity save(ContractEntity entity) {
        entity.setDeleted((short) 0);
        contractDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public ContractEntity update(ContractEntity entity) {
        if (entity.getDeleted() != null && entity.getDeleted() == 1) {
            contractDao.softDeleteById(entity.getId());
        } else {
            contractDao.updateById(entity);
        }
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        contractDao.deleteById(id);
    }

    @Override
    public Long getMinUnusedId() {
        return contractDao.selectMinUnusedId();
    }

    @Override
    public String generateContractNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "HT-" + dateStr + "-";
        LambdaQueryWrapper<ContractEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(ContractEntity::getContractNo, prefix)
               .orderByDesc(ContractEntity::getId)
               .last("LIMIT 1");
        ContractEntity lastContract = contractDao.selectOne(wrapper);
        int sequence = 1;
        if (lastContract != null && lastContract.getContractNo() != null) {
            String lastNo = lastContract.getContractNo();
            String lastSeqStr = lastNo.substring(lastNo.lastIndexOf("-") + 1);
            try {
                sequence = Integer.parseInt(lastSeqStr) + 1;
            } catch (NumberFormatException ignored) {}
        }
        return prefix + String.format("%04d", sequence);
    }

    @Override
    public ContractEntity getDetail(Long id) {
        return contractDao.selectById(id);
    }

    @Override
    public ContractDetailVO getDetailWithNames(Long id) {
        ContractEntity entity = contractDao.selectById(id);
        if (entity == null) {
            return null;
        }

        ContractDetailVO vo = new ContractDetailVO();
        vo.setId(entity.getId());
        vo.setContractNo(entity.getContractNo());
        vo.setCustomerId(entity.getCustomerId());
        vo.setSalesRepId(entity.getSalesRepId());
        vo.setDeptId(entity.getDeptId());
        vo.setZoneId(entity.getZoneId());
        vo.setProductId(entity.getProductId());
        vo.setContractAmount(entity.getContractAmount());
        vo.setActualLoanAmount(entity.getActualLoanAmount());
        vo.setServiceFeeRate(entity.getServiceFeeRate());
        vo.setServiceFee1(entity.getServiceFee1());
        vo.setServiceFee2(entity.getServiceFee2());
        vo.setServiceFee1Paid(entity.getServiceFee1Paid());
        vo.setServiceFee2Paid(entity.getServiceFee2Paid());
        vo.setServiceFee1PayDate(entity.getServiceFee1PayDate());
        vo.setServiceFee2PayDate(entity.getServiceFee2PayDate());
        vo.setStatus(entity.getStatus());
        vo.setSignDate(entity.getSignDate());
        vo.setPaperContractNo(entity.getPaperContractNo());
        vo.setFinanceSendTime(entity.getFinanceSendTime());
        vo.setFinanceReceiveTime(entity.getFinanceReceiveTime());
        vo.setLoanUse(entity.getLoanUse());
        vo.setGuaranteeInfo(entity.getGuaranteeInfo());
        vo.setRejectReason(entity.getRejectReason());
        vo.setRemark(entity.getRemark());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedBy(entity.getUpdatedBy());
        vo.setUpdatedAt(entity.getUpdatedAt());

        // 填充客户名称
        if (entity.getCustomerId() != null) {
            CustomerEntity customer = customerDao.selectById(entity.getCustomerId());
            if (customer != null) {
                vo.setCustomerName(customer.getName());
            }
        }

        // 填充销售代表名称 - 通过AuthFeignClient
        if (entity.getSalesRepId() != null) {
            Result<?> res = authFeignClient.getUserById(entity.getSalesRepId());
            if (res != null && res.getCode() == 200 && res.getData() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> user = (Map<String, Object>) res.getData();
                vo.setSalesRepName((String) user.get("realName"));
            }
        }

        // 填充部门名称 - 通过SystemFeignClient
        if (entity.getDeptId() != null) {
            Result<?> res = systemFeignClient.getDepartmentById(entity.getDeptId());
            if (res != null && res.getCode() == 200 && res.getData() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dept = (Map<String, Object>) res.getData();
                vo.setDeptName((String) dept.get("deptName"));
            }
        }

        // 填充战区名称 - 通过SystemFeignClient
        if (entity.getZoneId() != null) {
            Result<?> res = systemFeignClient.getZoneById(entity.getZoneId());
            if (res != null && res.getCode() == 200 && res.getData() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> zone = (Map<String, Object>) res.getData();
                vo.setZoneName((String) zone.get("zoneName"));
            }
        }

        // 填充产品名称 - 通过FinanceFeignClient
        if (entity.getProductId() != null) {
            Result<?> res = financeFeignClient.getById(entity.getProductId());
            if (res != null && res.getCode() == 200 && res.getData() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> product = (Map<String, Object>) res.getData();
                vo.setProductName((String) product.get("productName"));
            }
        }

        return vo;
    }

    @Override
    public Long count() {
        return contractDao.selectCount(null);
    }

    @Override
    public Long countByStatus(Short status) {
        LambdaQueryWrapper<ContractEntity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ContractEntity::getStatus, status);
        }
        return contractDao.selectCount(wrapper);
    }

    @Override
    @Transactional
    public void payFirstInstallment(Long id) {
        ContractEntity contract = contractDao.selectById(id);
        if (contract == null) {
            throw new IllegalArgumentException("合同不存在");
        }
        if (contract.getStatus() != 2) {
            throw new IllegalStateException("当前状态不允许操作，状态：" + contract.getStatus());
        }
        contract.setStatus((short) 3);
        contract.setServiceFee1Paid((short) 1);
        contractDao.updateById(contract);

        // 创建二期服务费记录
        Map<String, Object> serviceFeeRecord = new HashMap<>();
        serviceFeeRecord.put("id", financeFeignClient.getMinUnusedServiceFeeRecordId().getData());
        serviceFeeRecord.put("contractId", contract.getId());
        serviceFeeRecord.put("feeType", (short) 2);
        serviceFeeRecord.put("amount", contract.getServiceFee2());
        serviceFeeRecord.put("shouldAmount", contract.getServiceFee2());
        serviceFeeRecord.put("paymentStatus", (short) 1); // 支付状态：1-已支付
        serviceFeeRecord.put("paymentDate", Date.from(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).toInstant())); // 设置支付日期（北京时间）
        serviceFeeRecord.put("accountantId", 1); // 会计ID，必填字段，默认值为1
        serviceFeeRecord.put("deleted", (short) 0);
        financeFeignClient.createServiceFeeRecord(serviceFeeRecord);
    }

    @Override
    public void submitToFinance(Long id) {
        ContractEntity contract = contractDao.selectById(id);
        if (contract == null) {
            throw new IllegalArgumentException("合同不存在");
        }
        if (contract.getStatus() != 3) {
            throw new IllegalStateException("当前状态不允许操作，状态：" + contract.getStatus());
        }
        contract.setStatus((short) 4);
        contract.setFinanceSendTime(new Date());
        contractDao.updateById(contract);
    }

    @Transactional
    @Override
    public void bankLoan(Long id) {
        ContractEntity contract = contractDao.selectById(id);
        if (contract == null) {
            throw new IllegalArgumentException("合同不存在");
        }
        if (contract.getStatus() != 5) {
            throw new IllegalStateException("当前状态不允许操作，状态：" + contract.getStatus());
        }
        // 计算实际放款金额：合同金额 - 服务费1 - 服务费2
        BigDecimal actualLoanAmount = contract.getContractAmount()
                .subtract(contract.getServiceFee1() != null ? contract.getServiceFee1() : BigDecimal.ZERO)
                .subtract(contract.getServiceFee2() != null ? contract.getServiceFee2() : BigDecimal.ZERO);

        // 更新合同状态为已放款，并标记服务费2已支付，设置实际放款金额
        contract.setStatus((short) 7);
        contract.setServiceFee2Paid((short) 1);
        contract.setActualLoanAmount(actualLoanAmount);
        contractDao.updateById(contract);

        Date now = Date.from(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).toInstant());

        // 创建首期服务费记录
        Map<String, Object> serviceFeeRecord = new HashMap<>();
        serviceFeeRecord.put("id", financeFeignClient.getMinUnusedServiceFeeRecordId().getData());
        serviceFeeRecord.put("contractId", contract.getId());
        serviceFeeRecord.put("feeType", (short) 1);
        serviceFeeRecord.put("amount", contract.getServiceFee1());
        serviceFeeRecord.put("shouldAmount", contract.getServiceFee1());
        serviceFeeRecord.put("paymentStatus", (short) 1);
        serviceFeeRecord.put("paymentDate", now);
        serviceFeeRecord.put("accountantId", 1);
        serviceFeeRecord.put("deleted", (short) 0);
        financeFeignClient.createServiceFeeRecord(serviceFeeRecord);

        // 创建业绩记录（提成记录）：提成金额为合同金额的 1.5%
        BigDecimal commissionAmount = contract.getContractAmount().multiply(new BigDecimal("0.015"));
        PerformanceRecordEntity performanceRecord = new PerformanceRecordEntity();
        performanceRecord.setId(performanceRecordService.getMinUnusedId());
        performanceRecord.setContractId(contract.getId());
        performanceRecord.setSalesRepId(contract.getSalesRepId());
        performanceRecord.setDeptId(contract.getDeptId());
        performanceRecord.setZoneId(contract.getZoneId());
        performanceRecord.setContractAmount(contract.getContractAmount());
        performanceRecord.setCommissionRate(new BigDecimal("0.015"));
        performanceRecord.setCommissionAmount(commissionAmount);
        performanceRecord.setStatus((short) 1);
        performanceRecord.setCalculateTime(now);
        performanceRecord.setDeleted((short) 0);
        performanceRecordService.save(performanceRecord);

        // 创建提成发放记录：ID 从 commission_record 表查询，performance_id 绑定业绩记录 ID
        Map<String, Object> commissionRecord = new HashMap<>();
        commissionRecord.put("id", financeFeignClient.getMinUnusedCommissionRecordId().getData());
        commissionRecord.put("performanceId", performanceRecord.getId());
        commissionRecord.put("salesRepId", contract.getSalesRepId());
        commissionRecord.put("contractId", contract.getId());
        commissionRecord.put("commissionAmount", commissionAmount);
        commissionRecord.put("commissionRate", new BigDecimal("0.015"));
        commissionRecord.put("status", (short) 1);
        commissionRecord.put("createdAt", now);
        commissionRecord.put("deleted", (short) 0);
        financeFeignClient.createCommissionRecord(commissionRecord);
    }
}