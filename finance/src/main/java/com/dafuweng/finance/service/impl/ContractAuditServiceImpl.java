package com.dafuweng.finance.service.impl;

import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.finance.feign.SalesFeignClient;
import com.dafuweng.finance.service.ContractAuditService;
import com.dafuweng.sales.entity.vo.ContractDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContractAuditServiceImpl implements ContractAuditService {

    @Autowired
    private SalesFeignClient salesFeignClient;

    @Override
    public PageResponse<ContractDetailVO> pageList(int pageNum, int pageSize, Short status) {
        // 通过 Feign 调用 sales 服务查询审核中合同（带名称）
        return salesFeignClient.pageContractsByStatusWithNames(pageNum, pageSize, status).getData();
    }

    @Override
    public ContractDetailVO getContractDetail(Long contractId) {
        // 获取合同详情（带名称）
        var contractResult = salesFeignClient.getContractWithNames(contractId);
        if (contractResult == null || contractResult.getData() == null) {
            throw new RuntimeException("合同不存在");
        }
        return contractResult.getData();
    }

    @Override
    @Transactional
    public void approve(Long contractId, String auditOpinion) {
        // 将合同状态从 4 改为 5
        salesFeignClient.updateContractStatus(contractId, (short) 5);
    }

    @Override
    @Transactional
    public void reject(Long contractId, String rejectReason) {
        // 将合同状态从 4 改为 6，并设置拒绝原因
        salesFeignClient.updateContractStatusWithReason(contractId, (short) 6, rejectReason);
    }
}
