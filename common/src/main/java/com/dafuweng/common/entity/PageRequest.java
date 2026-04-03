package com.dafuweng.common.entity;

import lombok.Data;

@Data
public class PageRequest {
    private Integer page = 1;
    private Integer size = 10;
    private String sortField;
    private String sortOrder = "asc";
}
