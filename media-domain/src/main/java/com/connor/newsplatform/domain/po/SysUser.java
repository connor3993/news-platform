package com.connor.newsplatform.domain.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户（合并原 admin_user / app_user，通过 user_type 区分 admin / user）
 */
@Data
public class SysUser {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String nickname;
    private String avatar;
    private String phone;
    private String openid;
    private String userType;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
