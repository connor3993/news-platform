package com.connor.newsplatform.domain.vo;

import lombok.Data;

@Data
public class LoginVO {
    private Long id;
    private String username;
    private String name;
    private String nickname;
    private String avatar;
    private String token;
}
