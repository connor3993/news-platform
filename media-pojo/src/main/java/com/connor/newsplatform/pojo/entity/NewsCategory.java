package com.connor.newsplatform.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsCategory {
    private Long id;
    private String name;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
