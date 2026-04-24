package com.dafuweng.system.service;

import com.dafuweng.system.entity.SysZoneEntity;
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
import java.util.Map;

public interface SysZoneService {

    SysZoneEntity getById(Long id);

    PageResponse<SysZoneEntity> pageList(PageRequest request);

    List<SysZoneEntity> listAll();

    List<SysZoneEntity> listByStatus(Short status);

    @Transactional
    SysZoneEntity save(SysZoneEntity entity);

    @Transactional
    SysZoneEntity update(SysZoneEntity entity);

    @Transactional
    void delete(Long id);

    /**
     * 根据战区ID列表查询战区名称
     */
    Map<Long, String> listNamesByIds(List<Long> ids);

    /**
     * 批量设置战区的负责人姓名（通过 Feign 调用 auth 模块）
     */
    void fillDirectorNames(List<SysZoneEntity> zones);
}
