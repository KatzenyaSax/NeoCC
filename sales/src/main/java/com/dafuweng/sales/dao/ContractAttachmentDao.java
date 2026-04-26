package com.dafuweng.sales.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.sales.entity.ContractAttachmentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ContractAttachmentDao extends BaseMapper<ContractAttachmentEntity> {

    List<ContractAttachmentEntity> selectByContractId(@Param("contractId") Long contractId);

    @Select("SELECT COALESCE(MIN(t.id), 1) FROM (SELECT 1 as id UNION SELECT MAX(id) + 1 FROM contract_attachment) t WHERE NOT EXISTS (SELECT 1 FROM contract_attachment c WHERE c.id = t.id)")
    Long selectMinUnusedId();

    @Update("UPDATE contract_attachment SET deleted = 1 WHERE id = #{id}")
    int softDeleteById(@Param("id") Long id);
}
