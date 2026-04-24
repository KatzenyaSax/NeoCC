package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.entity.ContractEntity;
import com.dafuweng.sales.entity.vo.ContractDetailVO;
import com.dafuweng.sales.service.ContractService;
import com.dafuweng.sales.dao.ContractDao;
import com.dafuweng.sales.feign.AuthFeignClient;
import com.dafuweng.sales.feign.FinanceFeignClient;
import com.dafuweng.sales.feign.SystemFeignClient;
import com.dafuweng.sales.entity.CustomerEntity;
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
import java.time.format.DateTimeFormatter;
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

    @Override
    public ContractEntity getById(Long id) {
        return contractDao.selectById(id);
    }

    @Override
    public ContractEntity getByContractNo(String contractNo) {
        return contractDao.selectByContractNo(contractNo);
    }

    @Override
    public PageResponse<ContractEntity> pageList(PageRequest request) {
        IPage<ContractEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<ContractEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(ContractEntity::getId);
            } else {
                wrapper.orderByDesc(ContractEntity::getId);
            }
        } else {
            wrapper.orderByDesc(ContractEntity::getCreatedAt);
        }
        IPage<ContractEntity> result = contractDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent() , (int) page.getSize());
    }

    @Override
    public List<ContractEntity> listBySalesRepId(Long salesRepId) {
        LambdaQueryWrapper<ContractEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContractEntity::getSalesRepId, salesRepId);
        return contractDao.selectList(wrapper);
    }

    @Override
    public List<ContractEntity> listByStatus(Short status) {
        LambdaQueryWrapper<ContractEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContractEntity::getStatus, status);
        return contractDao.selectList(wrapper);
    }

    @Override
    public PageResponse<ContractEntity> pageListByStatus(int pageNum, int pageSize, Short status) {
        IPage<ContractEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ContractEntity> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(ContractEntity::getStatus, status);
        }

        wrapper.orderByDesc(ContractEntity::getCreatedAt);
        IPage<ContractEntity> result = contractDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    @Transactional
    public ContractEntity save(ContractEntity entity) {
        contractDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public ContractEntity update(ContractEntity entity) {
        contractDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        contractDao.deleteById(id);
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
}