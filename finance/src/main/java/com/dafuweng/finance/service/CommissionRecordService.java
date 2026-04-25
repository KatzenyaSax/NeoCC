package com.dafuweng.finance.service;

import com.dafuweng.finance.entity.CommissionRecordEntity;
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

public interface CommissionRecordService {

    CommissionRecordEntity getById(Long id);

    PageResponse<CommissionRecordEntity> pageList(PageRequest request);

    List<CommissionRecordEntity> listBySalesRepId(Long salesRepId);

    @Transactional
    CommissionRecordEntity save(CommissionRecordEntity entity);

    @Transactional
    CommissionRecordEntity update(CommissionRecordEntity entity);

@Transactional
    void delete(Long id);

    /** 确认提成 */
    @Transactional
    void confirm(Long id);

    /** 发放提成 */
    @Transactional
    void grant(Long id, String grantAccount, String remark);

    Long getMinUnusedId();
}
