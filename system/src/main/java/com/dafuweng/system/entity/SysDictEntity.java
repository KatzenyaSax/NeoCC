package com.dafuweng.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_dict")
public class SysDictEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String dictType;

    private String dictCode;

    private String dictLabel;

    private String dictValue;

    private Integer sortOrder;

    private Short status;

    private String remark;

    private Date createdAt;

    private Date updatedAt;

    @TableLogic
    private Short deleted;
}
