package com.dafuweng.sales.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.sales.entity.ContactRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ContactRecordDao extends BaseMapper<ContactRecordEntity> {

    List<ContactRecordEntity> selectByCustomerId(@Param("customerId") Long customerId);

    List<ContactRecordEntity> selectBySalesRepId(@Param("salesRepId") Long salesRepId);

    @Select("SELECT COALESCE(MIN(t.id + 1), 1) FROM (SELECT 1 as id UNION SELECT MAX(id) + 1 FROM contact_record) t WHERE NOT EXISTS (SELECT 1 FROM contact_record c WHERE c.id = t.id)")
    Long selectMinUnusedId();
}
