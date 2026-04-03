package com.dafuweng.common.entity;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    public static <T> Result<T> error400(String message) {
        return error(400, message);
    }

    public static <T> Result<T> error401(String message) {
        return error(401, message);
    }

    public static <T> Result<T> error403(String message) {
        return error(403, message);
    }

    public static <T> Result<T> error500(String message) {
        return error(500, message);
    }
}
