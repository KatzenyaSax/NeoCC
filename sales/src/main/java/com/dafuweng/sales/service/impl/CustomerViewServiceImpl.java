package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.entity.ContractEntity;
import com.dafuweng.sales.entity.CustomerEntity;
import com.dafuweng.sales.entity.CustomerTransferLogEntity;
import com.dafuweng.sales.entity.PerformanceRecordEntity;
import com.dafuweng.sales.entity.ContactRecordEntity;
import com.dafuweng.sales.dao.ContactRecordDao;
import com.dafuweng.sales.dao.ContractDao;
import com.dafuweng.sales.dao.CustomerDao;
import com.dafuweng.sales.dao.CustomerTransferLogDao;
import com.dafuweng.sales.dao.PerformanceRecordDao;
import com.dafuweng.sales.service.CustomerViewService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerViewServiceImpl implements CustomerViewService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private ContactRecordDao contactRecordDao;

    @Autowired
    private ContractDao contractDao;

    @Autowired
    private PerformanceRecordDao performanceRecordDao;

    @Autowired
    private CustomerTransferLogDao customerTransferLogDao;

    @Override
    public Map<String, Object> getCustomerView(Long id) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 客户基本信息
        CustomerEntity customer = customerDao.selectById(id);
        result.put("customer", customer);

        // 2. 联系记录
        List<ContactRecordEntity> contactRecords = contactRecordDao.selectByCustomerId(id);
        result.put("contactRecords", contactRecords);

        // 3. 合同记录
        LambdaQueryWrapper<ContractEntity> contractWrapper = new LambdaQueryWrapper<>();
        contractWrapper.eq(ContractEntity::getCustomerId, id);
        List<ContractEntity> contracts = contractDao.selectList(contractWrapper);
        result.put("contracts", contracts);

        // 4. 业绩记录（通过 contract.customerId = id 筛选）
        List<Long> contractIds = new ArrayList<>();
        for (ContractEntity c : contracts) {
            contractIds.add(c.getId());
        }
        List<PerformanceRecordEntity> performanceRecords = new ArrayList<>();
        if (!contractIds.isEmpty()) {
            LambdaQueryWrapper<PerformanceRecordEntity> perfWrapper = new LambdaQueryWrapper<>();
            perfWrapper.in(PerformanceRecordEntity::getContractId, contractIds);
            performanceRecords = performanceRecordDao.selectList(perfWrapper);
        }
        result.put("performanceRecords", performanceRecords);

        // 5. 转移记录
        List<CustomerTransferLogEntity> transferLogs = customerTransferLogDao.selectByCustomerId(id);
        result.put("transferLogs", transferLogs);

        return result;
    }
}
