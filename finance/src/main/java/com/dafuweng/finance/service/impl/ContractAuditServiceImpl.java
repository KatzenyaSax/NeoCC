package com.dafuweng.finance.service.impl;

import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.common.entity.vo.ContractDetailVO;
import com.dafuweng.common.entity.vo.ContractVO;
import com.dafuweng.finance.feign.SalesFeignClient;
import com.dafuweng.finance.service.ContractAuditService;
import com.dafuweng.sales.entity.ContractEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContractAuditServiceImpl implements ContractAuditService {

    @Autowired
    private SalesFeignClient salesFeignClient;

    @Override
    public PageResponse<ContractEntity> pageList(int pageNum, int pageSize, Short status) {
        // 通过 Feign 调用 sales 服务查询审核中的合同
        return salesFeignClient.pageContractsByStatus(pageNum, pageSize, status).getData();
    }

    @Override
    public ContractDetailVO getContractDetail(Long contractId) {
        ContractDetailVO detailVO = new ContractDetailVO();

        // 获取合同信息
        var contractResult = salesFeignClient.getContract(contractId);
        if (contractResult == null || contractResult.getData() == null) {
            throw new RuntimeException("合同不存在");
        }

        ContractVO contract = contractResult.getData();
        detailVO.setContract(contract);
        detailVO.setCustomerId(contract.getCustomerId());
        detailVO.setSalesRepId(contract.getSalesRepId());
        detailVO.setDeptId(contract.getDeptId());
        detailVO.setZoneId(contract.getZoneId());

        return detailVO;
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
