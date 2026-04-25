package com.dafuweng.sales.service;

import com.dafuweng.sales.entity.ContractAttachmentEntity;
import com.dafuweng.sales.dao.ContractAttachmentDao;
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

public interface ContractAttachmentService {

    ContractAttachmentEntity getById(Long id);

    PageResponse<ContractAttachmentEntity> pageList(PageRequest request);

    List<ContractAttachmentEntity> listByContractId(Long contractId);

    @Transactional
    ContractAttachmentEntity save(ContractAttachmentEntity entity);

    @Transactional
    ContractAttachmentEntity update(ContractAttachmentEntity entity);

    @Transactional
    void delete(Long id);

    Long getMinUnusedId();
}