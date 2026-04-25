package com.dafuweng.system.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.system.entity.SysDepartmentEntity;
import com.dafuweng.system.service.SysDepartmentService;
import com.dafuweng.system.dao.SysDepartmentDao;
import com.dafuweng.system.vo.DeptVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sysDepartment")
public class SysDepartmentController {

    @Autowired
    private SysDepartmentService sysDepartmentService;

    @Autowired
    private SysDepartmentDao sysDepartmentDao;

    @GetMapping("/{id}")
    public Result<SysDepartmentEntity> getById(@PathVariable Long id) {
        return Result.success(sysDepartmentService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<SysDepartmentEntity>> pageList(PageRequest request) {
        return Result.success(sysDepartmentService.pageList(request));
    }

    /**
     * 分页查询部门列表（带上级部门名称、战区名称、负责人姓名）
     */
    @GetMapping("/page/with-details")
    public Result<PageResponse<DeptVO>> pageListWithDetails(PageRequest request) {
        return Result.success(sysDepartmentService.pageListWithDetails(request));
    }

    @GetMapping("/listByParentId/{parentId}")
    public Result<List<SysDepartmentEntity>> listByParentId(@PathVariable Long parentId) {
        return Result.success(sysDepartmentService.listByParentId(parentId));
    }

    @GetMapping("/listByZoneId/{zoneId}")
    public Result<List<SysDepartmentEntity>> listByZoneId(@PathVariable Long zoneId) {
        return Result.success(sysDepartmentService.listByZoneId(zoneId));
    }

    /**
     * 查询所有部门（下拉用）
     */
    @GetMapping("/listAll")
    public Result<List<SysDepartmentEntity>> listAll() {
        LambdaQueryWrapper<SysDepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysDepartmentEntity::getSortOrder);
        return Result.success(sysDepartmentDao.selectList(wrapper));
    }

    @PostMapping
    public Result<SysDepartmentEntity> save(@RequestBody SysDepartmentEntity entity) {
        return Result.success(sysDepartmentService.save(entity));
    }

    @PutMapping
    public Result<SysDepartmentEntity> update(@RequestBody SysDepartmentEntity entity) {
        return Result.success(sysDepartmentService.update(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysDepartmentService.delete(id);
        return Result.success();
    }

    /**
     * 根据部门ID列表查询部门名称
     */
    @PostMapping("/names/by-ids")
    public Result<Map<Long, String>> listDeptNamesByIds(@RequestBody List<Long> ids) {
        return Result.success(sysDepartmentService.listNamesByIds(ids));
    }

    /**
     * 获取最小未使用的部门ID
     */
    @GetMapping("/min-unused-id")
    public Result<Long> getMinUnusedId() {
        return Result.success(sysDepartmentService.getMinUnusedId());
    }
}
