package com.dafuweng.finance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dafuweng.finance.entity.CommissionRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CommissionRecordDao extends BaseMapper<CommissionRecordEntity> {

    List<CommissionRecordEntity> selectBySalesRepId(@Param("salesRepId") Long salesRepId);

    @Select("SELECT COALESCE(MIN(t.id + 1), 1) FROM (SELECT 1 as id UNION SELECT MAX(id) + 1 FROM commission_record) t WHERE NOT EXISTS (SELECT 1 FROM commission_record c WHERE c.id = t.id AND c.deleted = 0)")
    Long selectMinUnusedId();

    @Update("UPDATE commission_record SET deleted = 1 WHERE id = #{id}")
    int softDeleteById(@Param("id") Long id);

    @Select("SELECT cr.*, u.real_name AS salesRepName, c.contract_no AS contractNo " +
            "FROM commission_record cr " +
            "LEFT JOIN sys_user u ON cr.sales_rep_id = u.id " +
            "LEFT JOIN contract c ON cr.contract_id = c.id " +
            "WHERE cr.deleted = 0 " +
            "ORDER BY cr.created_at DESC")
    IPage<CommissionRecordEntity> selectPageWithNames(@Param("page") IPage<CommissionRecordEntity> page);
}
