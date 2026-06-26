package com.connor.newsplatform.pojo.vo;

import lombok.Data;

@Data
public class AdminUserVO {
    private Long id;
    private String username;
    private String name;
    private String phone;
    private Integer status;
}
