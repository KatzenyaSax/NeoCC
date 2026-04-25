package com.dafuweng.finance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.finance.entity.FinanceProductEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FinanceProductDao extends BaseMapper<FinanceProductEntity> {

    List<FinanceProductEntity> selectByBankId(@Param("bankId") Long bankId);

    @Select("SELECT COALESCE(MIN(t.id + 1), 1) FROM (SELECT 1 as id UNION SELECT MAX(id) + 1 FROM finance_product) t WHERE NOT EXISTS (SELECT 1 FROM finance_product f WHERE f.id = t.id)")
    Long selectMinUnusedId();
}
