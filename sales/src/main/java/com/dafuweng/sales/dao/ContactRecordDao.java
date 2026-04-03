package com.dafuweng.sales.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.sales.entity.ContactRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContactRecordDao extends BaseMapper<ContactRecordEntity> {

    List<ContactRecordEntity> selectByCustomerId(@Param("customerId") Long customerId);

    List<ContactRecordEntity> selectBySalesRepId(@Param("salesRepId") Long salesRepId);
}
