package com.connor.newsplatform.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryVO {
    private Long id;
    private String name;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
