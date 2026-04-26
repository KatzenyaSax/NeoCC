package com.dafuweng.finance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.finance.entity.ServiceFeeRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ServiceFeeRecordDao extends BaseMapper<ServiceFeeRecordEntity> {

    List<ServiceFeeRecordEntity> selectByContractId(@Param("contractId") Long contractId);

    @Select("SELECT COALESCE(MIN(t.id), 1) FROM (SELECT 1 as id UNION SELECT MAX(id) + 1 FROM service_fee_record) t WHERE NOT EXISTS (SELECT 1 FROM service_fee_record s WHERE s.id = t.id)")
    Long selectMinUnusedId();

    @Update("UPDATE service_fee_record SET deleted = 1 WHERE id = #{id}")
    int softDeleteById(@Param("id") Long id);
}
