package com.dafuweng.finance.service;

import com.dafuweng.finance.entity.BankEntity;
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

public interface BankService {

    BankEntity getById(Long id);

    PageResponse<BankEntity> pageList(PageRequest request);

    List<BankEntity> listByStatus(Short status);

    @Transactional
    BankEntity save(BankEntity entity);

    @Transactional
    BankEntity update(BankEntity entity);

    @Transactional
    void delete(Long id);
}
