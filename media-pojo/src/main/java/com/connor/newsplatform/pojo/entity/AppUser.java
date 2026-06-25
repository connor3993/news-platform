package com.connor.newsplatform.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppUser {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
