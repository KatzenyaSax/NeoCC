package com.dafuweng.finance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.finance.entity.LoanAuditEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LoanAuditDao extends BaseMapper<LoanAuditEntity> {

    LoanAuditEntity selectByContractId(@Param("contractId") Long contractId);

    @Select("SELECT COALESCE(MIN(t.id + 1), 1) FROM (SELECT 1 as id UNION SELECT MAX(id) + 1 FROM loan_audit) t WHERE NOT EXISTS (SELECT 1 FROM loan_audit l WHERE l.id = t.id AND l.deleted = 0)")
    Long selectMinUnusedId();

    @Update("UPDATE loan_audit SET deleted = 1 WHERE id = #{id}")
    int softDeleteById(@Param("id") Long id);
}
