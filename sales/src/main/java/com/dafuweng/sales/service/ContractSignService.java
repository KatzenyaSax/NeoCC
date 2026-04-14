package com.dafuweng.sales.service;

import com.dafuweng.sales.entity.ContractEntity;

/**
 * 合同签署服务 — 专门处理合同签署事件发送
 */
public interface ContractSignService {

    /**
     * 签署合同并发送事件通知金融部
     */
    void sign(Long contractId);
}
