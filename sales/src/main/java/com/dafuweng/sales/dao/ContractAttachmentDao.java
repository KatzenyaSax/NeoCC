package com.dafuweng.sales.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.sales.entity.ContractAttachmentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContractAttachmentDao extends BaseMapper<ContractAttachmentEntity> {

    List<ContractAttachmentEntity> selectByContractId(@Param("contractId") Long contractId);
}
