package com.dafuweng.finance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.finance.entity.LoanAuditRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LoanAuditRecordDao extends BaseMapper<LoanAuditRecordEntity> {

    List<LoanAuditRecordEntity> selectByLoanAuditId(@Param("loanAuditId") Long loanAuditId);
}
