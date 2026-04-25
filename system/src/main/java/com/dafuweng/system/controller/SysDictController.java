package com.dafuweng.system.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.system.entity.SysDictEntity;
import com.dafuweng.system.service.SysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sysDict")
public class SysDictController {

    @Autowired
    private SysDictService sysDictService;

    @GetMapping("/{id}")
    public Result<SysDictEntity> getById(@PathVariable Long id) {
        return Result.success(sysDictService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<SysDictEntity>> pageList(PageRequest request) {
        return Result.success(sysDictService.pageList(request));
    }

    @GetMapping("/listByDictType")
    public Result<List<SysDictEntity>> listByDictType(@RequestParam String dictType) {
        return Result.success(sysDictService.listByDictType(dictType));
    }

    @PostMapping
    public Result<SysDictEntity> save(@RequestBody SysDictEntity entity) {
        return Result.success(sysDictService.save(entity));
    }

    @PutMapping
    public Result<SysDictEntity> update(@RequestBody SysDictEntity entity) {
        return Result.success(sysDictService.update(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysDictService.delete(id);
        return Result.success();
    }

    /**
     * 获取最小未使用的字典ID
     */
    @GetMapping("/min-unused-id")
    public Result<Long> getMinUnusedId() {
        return Result.success(sysDictService.getMinUnusedId());
    }
}
