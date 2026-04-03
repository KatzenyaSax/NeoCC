package com.dafuweng.system.service;

import com.dafuweng.system.entity.SysOperationLogEntity;
import com.dafuweng.system.dao.SysOperationLogDao;
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

public interface SysOperationLogService {

    SysOperationLogEntity getById(Long id);

    PageResponse<SysOperationLogEntity> pageList(PageRequest request);

    List<SysOperationLogEntity> listByUserId(Long userId);

    List<SysOperationLogEntity> listByModule(String module);

    @Transactional
    SysOperationLogEntity save(SysOperationLogEntity entity);
}
