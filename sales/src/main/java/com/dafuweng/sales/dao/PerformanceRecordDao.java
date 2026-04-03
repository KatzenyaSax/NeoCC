package com.dafuweng.sales.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.sales.entity.PerformanceRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PerformanceRecordDao extends BaseMapper<PerformanceRecordEntity> {

    PerformanceRecordEntity selectByContractId(@Param("contractId") Long contractId);
}
