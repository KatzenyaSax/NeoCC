package com.dafuweng.auth.service.impl;

import com.dafuweng.auth.dao.SysPermissionDao;
import com.dafuweng.auth.entity.SysPermissionEntity;
import com.dafuweng.auth.service.SysPermissionService;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysPermissionServiceImpl implements SysPermissionService {

    @Autowired
    private SysPermissionDao sysPermissionDao;

    @Override
    public SysPermissionEntity getById(Long id) {
        return sysPermissionDao.selectById(id);
    }

    @Override
    public PageResponse<SysPermissionEntity> pageList(PageRequest request) {
        IPage<SysPermissionEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysPermissionEntity> wrapper = new LambdaQueryWrapper<>();

        // 搜索条件：权限名称
        if (StringUtils.hasText(request.getPermName())) {
            wrapper.like(SysPermissionEntity::getPermName, request.getPermName());
        }
        // 搜索条件：权限编码
        if (StringUtils.hasText(request.getPermCode())) {
            wrapper.like(SysPermissionEntity::getPermCode, request.getPermCode());
        }
        // 搜索条件：权限类型
        if (request.getPermType() != null) {
            wrapper.eq(SysPermissionEntity::getPermType, request.getPermType());
        }

        // 排序
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(SysPermissionEntity::getId);
            } else {
                wrapper.orderByDesc(SysPermissionEntity::getId);
            }
        } else {
            wrapper.orderByAsc(SysPermissionEntity::getSortOrder);
        }
        IPage<SysPermissionEntity> result = sysPermissionDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public List<SysPermissionEntity> listByStatus(Short status) {
        LambdaQueryWrapper<SysPermissionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermissionEntity::getStatus, status);
        wrapper.orderByAsc(SysPermissionEntity::getSortOrder);
        return sysPermissionDao.selectList(wrapper);
    }

    @Override
    public List<SysPermissionEntity> treeList() {
        LambdaQueryWrapper<SysPermissionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysPermissionEntity::getSortOrder);
        return sysPermissionDao.selectList(wrapper);
    }

    @Override
    public List<SysPermissionEntity> listByParentId(Long parentId) {
        LambdaQueryWrapper<SysPermissionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermissionEntity::getParentId, parentId);
        wrapper.orderByAsc(SysPermissionEntity::getSortOrder);
        return sysPermissionDao.selectList(wrapper);
    }

    @Override
    @Transactional
    public SysPermissionEntity save(SysPermissionEntity entity) {
        sysPermissionDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public SysPermissionEntity update(SysPermissionEntity entity) {
        sysPermissionDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 递归删除子节点
        deleteRecursively(id);
        sysPermissionDao.deleteById(id);
    }

    private void deleteRecursively(Long parentId) {
        List<SysPermissionEntity> children = listByParentId(parentId);
        if (children != null && !children.isEmpty()) {
            for (SysPermissionEntity child : children) {
                deleteRecursively(child.getId());
                sysPermissionDao.deleteById(child.getId());
            }
        }
    }
}
