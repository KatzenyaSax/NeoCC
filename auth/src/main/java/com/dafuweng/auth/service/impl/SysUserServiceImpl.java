package com.dafuweng.auth.service.impl;

import com.dafuweng.auth.dao.SysPermissionDao;
import com.dafuweng.auth.dao.SysRolePermissionDao;
import com.dafuweng.auth.dao.SysUserDao;
import com.dafuweng.auth.dao.SysUserRoleDao;
import com.dafuweng.auth.entity.SysUserEntity;
import com.dafuweng.auth.entity.SysUserRoleEntity;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.auth.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserDao sysUserDao;

    @Autowired
    private SysUserRoleDao sysUserRoleDao;

    @Autowired
    private SysRolePermissionDao sysRolePermissionDao;

    @Autowired
    private SysPermissionDao sysPermissionDao;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public SysUserEntity getById(Long id) {
        SysUserEntity user = sysUserDao.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public PageResponse<SysUserEntity> pageList(PageRequest request) {
        IPage<SysUserEntity> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getSortField())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(SysUserEntity::getId);
            } else {
                wrapper.orderByDesc(SysUserEntity::getId);
            }
        } else {
            wrapper.orderByDesc(SysUserEntity::getCreatedAt);
        }
        IPage<SysUserEntity> result = sysUserDao.selectPage(page, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return PageResponse.of(result.getTotal(), result.getRecords(),
            (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public SysUserEntity getByUsername(String username) {
        return sysUserDao.selectByUsername(username);
    }

    @Override
    @Transactional
    public SysUserEntity login(String username, String password, String loginIp) {
        SysUserEntity user = sysUserDao.selectByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        // 保存密码 hash（登录成功后要清除，避免返回给前端）
        String passwordHash = user.getPassword();
        // 检查账号锁定
        if (user.getLockTime() != null && user.getLockTime().after(new Date())) {
            throw new IllegalArgumentException("账号已锁定，请稍后再试");
        }
        // 检查删除状态
        if (Objects.equals(user.getDeleted(), (short) 1)) {
            throw new IllegalArgumentException("账号已删除");
        }
        // 验证密码（BCrypt）
        if (!passwordEncoder.matches(password, passwordHash)) {
            int errors = (user.getLoginErrorCount() == null ? 0 : user.getLoginErrorCount()) + 1;
            user.setLoginErrorCount(errors);
            if (errors >= 5) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MINUTE, 30);
                user.setLockTime(cal.getTime());
            }
            sysUserDao.updateById(user);
            throw new IllegalArgumentException("用户名或密码错误");
        }
        // 登录成功，重置错误计数（用 LambdaUpdateWrapper，避免更新 password 字段）
        sysUserDao.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysUserEntity>()
                        .eq(SysUserEntity::getId, user.getId())
                        .set(SysUserEntity::getLoginErrorCount, 0)
                        .set(SysUserEntity::getLastLoginTime, new Date())
                        .set(SysUserEntity::getLastLoginIp, loginIp));
        // 不返回明文密码
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        // 登出只需要清除最后登录信息
        SysUserEntity user = sysUserDao.selectById(userId);
        if (user != null) {
            user.setLastLoginTime(null);
            user.setLastLoginIp(null);
            sysUserDao.updateById(user);
        }
    }

    @Override
    @Transactional
    public void unlock(Long userId) {
        SysUserEntity user = sysUserDao.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setLockTime(null);
        user.setLoginErrorCount(0);
        sysUserDao.updateById(user);
    }

    @Override
    public List<String> getPermCodesByUserId(Long userId) {
        // 先获取用户的所有角色
        List<Long> roleIds = sysUserRoleDao.selectRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        // 汇总所有角色的权限码
        List<String> allPermCodes = new ArrayList<>();
        for (Long roleId : roleIds) {
            List<String> codes = sysPermissionDao.selectPermCodesByRoleId(roleId);
            if (codes != null) {
                allPermCodes.addAll(codes);
            }
        }
        return allPermCodes;
    }

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        List<Long> roleIds = sysUserRoleDao.selectRoleIdsByUserId(userId);
        return roleIds == null ? new ArrayList<>() : roleIds;
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        sysUserRoleDao.deleteByUserId(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRoleEntity> userRoles = new ArrayList<>();
            for (Long roleId : roleIds) {
                SysUserRoleEntity ur = new SysUserRoleEntity();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                ur.setCreatedAt(new Date());
                userRoles.add(ur);
            }
            sysUserRoleDao.insertBatch(userRoles);
        }
    }

    @Override
    @Transactional
    public SysUserEntity save(SysUserEntity entity) {
        sysUserDao.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public SysUserEntity update(SysUserEntity entity) {
        sysUserDao.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sysUserDao.deleteById(id);
    }

    @Override
    public List<SysUserEntity> listSalesReps() {
        List<SysUserEntity> list = sysUserDao.selectByRoleCode("sales_rep");
        list.forEach(u -> u.setPassword(null));
        return list;
    }

    @Override
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        SysUserEntity user = sysUserDao.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserDao.updateById(user);
        return true;
    }

    /** 调试用：重置指定用户的密码（不校验旧密码） */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        LambdaUpdateWrapper<SysUserEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUserEntity::getId, userId)
               .set(SysUserEntity::getPassword, passwordEncoder.encode(newPassword));
        sysUserDao.update(null, wrapper);
    }
}
