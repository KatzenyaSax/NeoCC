package com.dafuweng.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.system.entity.SysDictEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysDictDao extends BaseMapper<SysDictEntity> {

    List<SysDictEntity> selectByDictType(@Param("dictType") String dictType);

    @Select("SELECT MIN(t1.id + 1) FROM sys_dict t1 WHERE NOT EXISTS (SELECT 1 FROM sys_dict t2 WHERE t2.id = t1.id + 1 AND t2.deleted = 0)")
    Long selectMinUnusedId();
}
