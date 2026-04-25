package com.dafuweng.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.system.entity.SysDepartmentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysDepartmentDao extends BaseMapper<SysDepartmentEntity> {

    @Select("SELECT MIN(t1.id + 1) FROM sys_department t1 WHERE NOT EXISTS (SELECT 1 FROM sys_department t2 WHERE t2.id = t1.id + 1 AND t2.deleted = 0)")
    Long selectMinUnusedId();
}
