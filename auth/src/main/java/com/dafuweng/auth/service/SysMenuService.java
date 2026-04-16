package com.dafuweng.auth.service;

import com.dafuweng.auth.entity.SysMenuEntity;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface SysMenuService {
    
    /**
     * 获取用户菜单树
     */
    List<SysMenuEntity> getUserMenus(Long userId);
}
