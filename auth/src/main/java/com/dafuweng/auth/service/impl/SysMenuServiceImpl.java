package com.dafuweng.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dafuweng.auth.entity.SysMenuEntity;
import com.dafuweng.auth.mapper.SysMenuMapper;
import com.dafuweng.auth.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 */
@Service
public class SysMenuServiceImpl implements SysMenuService {
    
    @Autowired
    private SysMenuMapper sysMenuMapper;
    
    @Override
    public List<SysMenuEntity> getUserMenus(Long userId) {
        // 查询所有菜单（包括一级和二级）
        // 直接返回所有菜单，在 Controller层组装树形结构
        return sysMenuMapper.selectList(null);
    }
    
    /**
     * 构建菜单树
     */
    private List<SysMenuEntity> buildMenuTree(List<SysMenuEntity> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> menu.getParentId().equals(parentId))
                .peek(menu -> {
                    // 递归设置子菜单
                    List<SysMenuEntity> children = buildMenuTree(menus, menu.getMenuId());
                    if (!children.isEmpty()) {
                        // 这里可以通过扩展实体类或使用DTO来设置children
                        // 当前简化处理，在Controller层转换
                    }
                })
                .collect(Collectors.toList());
    }
}
