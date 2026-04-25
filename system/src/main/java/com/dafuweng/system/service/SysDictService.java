package com.dafuweng.system.service;

import com.dafuweng.system.entity.SysDictEntity;
import com.dafuweng.system.dao.SysDictDao;
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

public interface SysDictService {

    SysDictEntity getById(Long id);

    PageResponse<SysDictEntity> pageList(PageRequest request);

    List<SysDictEntity> listByDictType(String dictType);

    @Transactional
    SysDictEntity save(SysDictEntity entity);

    @Transactional
    SysDictEntity update(SysDictEntity entity);

    @Transactional
    void delete(Long id);

    /**
     * 获取最小未使用的字典ID
     */
    Long getMinUnusedId();
}
