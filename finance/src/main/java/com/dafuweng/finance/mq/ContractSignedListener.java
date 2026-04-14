package com.dafuweng.finance.mq;

import com.dafuweng.common.mq.MqConfig;
import com.dafuweng.common.mq.event.ContractSignedEvent;
import com.dafuweng.finance.entity.LoanAuditEntity;
import com.dafuweng.finance.entity.LoanAuditRecordEntity;
import com.dafuweng.finance.service.LoanAuditRecordService;
import com.dafuweng.finance.service.LoanAuditService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 接收 sales 服务签署合同事件，自动创建贷款审核记录
 */
@Component
public class ContractSignedListener {

    @Autowired
    private LoanAuditService loanAuditService;

    @Autowired
    private LoanAuditRecordService loanAuditRecordService;

    @RabbitListener(queues = MqConfig.QUEUE_CONTRACT_SIGNED)
    public void onContractSigned(ContractSignedEvent event) {
        // 检查是否已存在该合同的审核记录（幂等）
        LoanAuditEntity existing = loanAuditService.getByContractId(event.getContractId());
        if (existing != null) {
            return;
        }

        // 创建贷款审核记录，初始状态 audit_status=1（待接收）
        LoanAuditEntity audit = new LoanAuditEntity();
        audit.setContractId(event.getContractId());
        audit.setAuditStatus((short) 1);
        audit.setCreatedAt(new Date());
        audit.setUpdatedAt(new Date());
        loanAuditService.save(audit);

        // 记录接收轨迹（action=receive，operator_id=0 表示系统自动）
        LoanAuditRecordEntity record = new LoanAuditRecordEntity();
        record.setLoanAuditId(audit.getId());
        record.setAction("receive");
        record.setOperatorId(0L);
        record.setOperatorName("系统");
        record.setOperatorRole("system");
        record.setContent("合同签署自动创建审核任务");
        record.setCreatedAt(new Date());
        loanAuditRecordService.save(record);
    }
}
