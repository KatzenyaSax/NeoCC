package com.dafuweng.common.exception;

import com.dafuweng.common.entity.Result;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MybatisPlusException.class)
    public Result<?> handleMybatisPlusException(MybatisPlusException e) {
        return Result.error400("数据操作失败: " + e.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<?> handleDuplicateKeyException(DuplicateKeyException e) {
        return Result.error400("数据重复，请检查唯一约束: " + e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.error400("参数错误: " + e.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    public Result<?> handleNullPointerException(NullPointerException e) {
        return Result.error500("空指针异常: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error500("系统异常: " + e.getMessage());
    }
}
