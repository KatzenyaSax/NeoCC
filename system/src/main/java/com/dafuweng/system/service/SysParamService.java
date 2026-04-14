package com.dafuweng.system.service;

import com.dafuweng.system.entity.SysParamEntity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

public interface SysParamService {

    SysParamEntity getById(Long id);

    SysParamEntity getByParamKey(String paramKey);

    String getParamValue(String paramKey);

    PageResponse<SysParamEntity> pageList(PageRequest request);

    List<SysParamEntity> listByParamGroup(String paramGroup);

    @Transactional
    SysParamEntity save(SysParamEntity entity);

    @Transactional
    SysParamEntity update(SysParamEntity entity);

    @Transactional
    void delete(Long id);
}
