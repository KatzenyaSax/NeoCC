package com.dafuweng.sales.service;

import com.dafuweng.sales.entity.WorkLogEntity;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface WorkLogService {

    WorkLogEntity getById(Long id);

    PageResponse<WorkLogEntity> pageList(PageRequest request);

    List<WorkLogEntity> listBySalesRepId(Long salesRepId);

    List<WorkLogEntity> listBySalesRepIds(List<Long> salesRepIds);

    boolean isDuplicate(Long salesRepId, String logDate);

    @Transactional
    WorkLogEntity save(WorkLogEntity entity);

    @Transactional
    WorkLogEntity update(WorkLogEntity entity);

    @Transactional
    void delete(Long id);

    Long getMinUnusedId();
}