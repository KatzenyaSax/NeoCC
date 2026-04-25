package com.dafuweng.system.service;

import com.dafuweng.system.entity.SysZoneEntity;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import org.springframework.transaction.annotation.Transactional;

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
     * 获取最小未使用的战区ID
     */
    Long getMinUnusedId();

    /**
     * 根据战区ID列表查询战区名称
     */
    Map<Long, String> listNamesByIds(List<Long> ids);

    /**
     * 批量设置战区的负责人姓名（通过 Feign 调用 auth 模块）
     */
    void fillDirectorNames(List<SysZoneEntity> zones);
}
