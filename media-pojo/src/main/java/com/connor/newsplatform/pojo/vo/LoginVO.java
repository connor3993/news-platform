package com.connor.newsplatform.pojo.vo;

import lombok.Data;

@Data
public class LoginVO {
    private Long id;
    private String username;
    private String name;
    private String role;
    private String nickname;
    private String avatar;
    private String token;
}
