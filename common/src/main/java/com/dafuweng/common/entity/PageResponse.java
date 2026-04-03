package com.dafuweng.common.entity;

import lombok.Data;
import java.util.List;

@Data
public class PageResponse<T> {
    private Long total;
    private List<T> records;
    private Integer page;
    private Integer size;

    public static <T> PageResponse<T> of(Long total, List<T> records, Integer page, Integer size) {
        PageResponse<T> r = new PageResponse<>();
        r.setTotal(total);
        r.setRecords(records);
        r.setPage(page);
        r.setSize(size);
        return r;
    }
}
