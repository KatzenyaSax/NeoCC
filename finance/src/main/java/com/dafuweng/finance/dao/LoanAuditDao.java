package com.dafuweng.finance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.finance.entity.LoanAuditEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoanAuditDao extends BaseMapper<LoanAuditEntity> {

    LoanAuditEntity selectByContractId(@Param("contractId") Long contractId);
}
