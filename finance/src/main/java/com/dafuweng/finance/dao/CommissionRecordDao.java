package com.dafuweng.finance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.finance.entity.CommissionRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommissionRecordDao extends BaseMapper<CommissionRecordEntity> {

    List<CommissionRecordEntity> selectBySalesRepId(@Param("salesRepId") Long salesRepId);
}
