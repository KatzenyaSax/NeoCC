package com.dafuweng.finance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.finance.entity.FinanceProductEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FinanceProductDao extends BaseMapper<FinanceProductEntity> {

    List<FinanceProductEntity> selectByBankId(@Param("bankId") Long bankId);
}
