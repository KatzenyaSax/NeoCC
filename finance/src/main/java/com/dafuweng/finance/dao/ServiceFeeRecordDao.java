package com.dafuweng.finance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.finance.entity.ServiceFeeRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServiceFeeRecordDao extends BaseMapper<ServiceFeeRecordEntity> {

    List<ServiceFeeRecordEntity> selectByContractId(@Param("contractId") Long contractId);
}
