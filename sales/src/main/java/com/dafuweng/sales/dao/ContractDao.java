package com.dafuweng.sales.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.sales.entity.ContractEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ContractDao extends BaseMapper<ContractEntity> {

    ContractEntity selectByContractNo(@Param("contractNo") String contractNo);
}
