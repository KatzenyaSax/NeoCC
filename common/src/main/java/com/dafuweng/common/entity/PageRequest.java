package com.dafuweng.common.entity;

import lombok.Data;

@Data
public class PageRequest {
    private Integer page = 1;
    private Integer size = 10;
    @com.fasterxml.jackson.annotation.JsonProperty("pageNum")
    private Integer pageNum;
    @com.fasterxml.jackson.annotation.JsonProperty("pageSize")
    private Integer pageSize;
    private String sortField;
    private String sortOrder = "asc";
    private String name;

    public Integer getPage() {
        return pageNum != null ? pageNum : page;
    }

    public Integer getSize() {
        return pageSize != null ? pageSize : size;
    }
}
