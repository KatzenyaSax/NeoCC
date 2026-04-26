package com.dafuweng.finance.service;

import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.sales.entity.vo.ContractDetailVO;
import com.dafuweng.sales.entity.ContractEntity;

/**
 * 合同审核服务接口
 * 仅处理 contract.status = 4（审核中）的合同
 */
public interface ContractAuditService {

    /**
     * 分页查询审核中的合同（status = 4，含关联名称）
     */
    PageResponse<ContractDetailVO> pageList(int pageNum, int pageSize, Short status);

    /**
     * 获取合同详情（含关联信息和名称）
     */
    ContractDetailVO getContractDetail(Long contractId);

    /**
     * 通过审核
     * 将合同状态从 4 改为 5
     */
    void approve(Long contractId, String auditOpinion);

    /**
     * 拒绝审核
     * 将合同状态从 4 改为 6
     */
    void reject(Long contractId, String rejectReason);
}
