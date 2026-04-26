package com.dafuweng.finance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dafuweng.finance.entity.FinanceProductEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FinanceProductDao extends BaseMapper<FinanceProductEntity> {

    List<FinanceProductEntity> selectByBankId(@Param("bankId") Long bankId);

    @Select("SELECT COALESCE(MIN(t.id), 1) FROM (SELECT 1 as id UNION SELECT MAX(id) + 1 FROM finance_product) t WHERE NOT EXISTS (SELECT 1 FROM finance_product f WHERE f.id = t.id)")
    Long selectMinUnusedId();

    @Update("UPDATE finance_product SET deleted = 1 WHERE id = #{id}")
    int softDeleteById(@Param("id") Long id);

    @Select("SELECT fp.*, b.bank_name AS bankName " +
            "FROM finance_product fp " +
            "LEFT JOIN bank b ON fp.bank_id = b.id " +
            "WHERE fp.deleted = 0 " +
            "ORDER BY fp.created_at DESC")
    IPage<FinanceProductEntity> selectPageWithBank(@Param("page") IPage<FinanceProductEntity> page);
}
