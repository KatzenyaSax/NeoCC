package com.dafuweng.common.entity;

import lombok.Data;

@Data
public class PageRequest {
    private Integer page = 1;
    private Integer size = 10;
    private String sortField;
    private String sortOrder = "asc";

    /** 兼容 RuoYi 前端 pageNum 参数 */
    public void setPageNum(Integer pageNum) {
        if (pageNum != null) this.page = pageNum;
    }

    /** 兼容 RuoYi 前端 pageSize 参数 */
    public void setPageSize(Integer pageSize) {
        if (pageSize != null) this.size = pageSize;
    }
}
