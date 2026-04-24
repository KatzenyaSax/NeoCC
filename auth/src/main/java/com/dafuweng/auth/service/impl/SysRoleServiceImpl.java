package com.dafuweng.auth.service.impl;

import com.dafuweng.auth.dao.SysRoleDao;
import com.dafuweng.auth.dao.SysRolePermissionDao;
import com.dafuweng.auth.dao.SysUserRoleDao;
import com.dafuweng.auth.entity.SysRoleEntity;
import com.dafuweng.auth.entity.SysRolePermissionEntity;
import com.dafuweng.auth.service.SysRoleService;
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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl implements SysRoleService {

    @Autowired
    private SysRoleDao sysRoleDao;

    @Autowired
    private SysRolePermissionDao sysRolePermissionDao;

    @Autowired
    private SysUserRoleDao sysUserRoleDao;

    @Override
    public SysRoleEntity getById(Long id) {
        return sysRoleDao.selectById(id);
    }

    @Override
    public PageResponse<SysRoleEntity> pageList(PageRequest request) {
        IPage<SysRoleEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysRoleEntity> wrapper = new LambdaQueryWrapper<>();

        // 搜索条件：角色名称
        if (StringUtils.hasText(request.getRoleName())) {
            wrapper.like(SysRoleEntity::getRoleName, request.getRoleName());
        }
        // 搜索条件：角色编码
        if (StringUtils.hasText(request.getRoleCode())) {
            wrapper.like(SysRoleEntity::getRoleCode, request.getRoleCode());
        }

        // 排序
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(SysRoleEntity::getId);
            } else {
                wrapper.orderByDesc(SysRoleEntity::getId);
            }
        } else {
            wrapper.orderByDesc(SysRoleEntity::getCreatedAt);
        }
        IPage<SysRoleEntity> result = sysRoleDao.selectPage(page, wrapper);
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public List<SysRoleEntity> listByStatus(Short status) {
        LambdaQueryWrapper<SysRoleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleEntity::getStatus, status);
        return sysRoleDao.selectList(wrapper);
    }

    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        List<Long> permissionIds = sysRolePermissionDao.selectPermissionIdsByRoleId(roleId);
        return permissionIds == null ? new ArrayList<>() : permissionIds;
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        sysRolePermissionDao.deleteByRoleId(roleId);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<SysRolePermissionEntity> rolePermissions = new ArrayList<>();
            for (Long permissionId : permissionIds) {
                SysRolePermissionEntity rp = new SysRolePermissionEntity();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rp.setCreatedAt(new Date());
                rolePermissions.add(rp);
            }
            sysRolePermissionDao.insertBatch(rolePermissions);
        }
    }

    @Override
    @Transactional
    public SysRoleEntity save(SysRoleEntity entity) {
        sysRoleDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public SysRoleEntity update(SysRoleEntity entity) {
        sysRoleDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sysRoleDao.deleteById(id);
    }

    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        List<Long> roleIds = sysUserRoleDao.selectRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) return new ArrayList<>();
        return roleIds.stream()
            .map(id -> {
                SysRoleEntity role = sysRoleDao.selectById(id);
                return role != null ? role.getRoleCode() : null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
