package com.dafuweng.sales.service.impl;

import com.dafuweng.common.mq.MqConfig;
import com.dafuweng.common.mq.event.ContractSignedEvent;
import com.dafuweng.sales.entity.ContractEntity;
import com.dafuweng.sales.service.ContractSignService;
import com.dafuweng.sales.dao.ContractDao;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class ContractSignServiceImpl implements ContractSignService {

    @Autowired
    private ContractDao contractDao;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public void sign(Long contractId) {
        ContractEntity contract = contractDao.selectById(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("合同不存在");
        }
        if (contract.getStatus() != 1) {
            throw new IllegalStateException("当前状态不允许签署，当前状态：" + contract.getStatus());
        }

        // 更新合同状态为已签署（status=2）
        contract.setStatus((short) 2);
        contract.setSignDate(new Date());
        contractDao.updateById(contract);

        // 发送事件给金融部
        ContractSignedEvent event = new ContractSignedEvent();
        event.setContractId(contract.getId());
        event.setCustomerId(contract.getCustomerId());
        event.setSalesRepId(contract.getSalesRepId());
        event.setDeptId(contract.getDeptId());
        event.setContractAmount(contract.getContractAmount());
        event.setSignDate(contract.getSignDate());

        rabbitTemplate.convertAndSend(
            MqConfig.EXCHANGE_SALES,
            MqConfig.ROUTING_CONTRACT_SIGNED,
            event
        );
    }
}
