package com.connor.newsplatform.server.service;

import com.connor.newsplatform.pojo.dto.LoginDTO;
import com.connor.newsplatform.pojo.dto.UserProfileDTO;
import com.connor.newsplatform.pojo.dto.UserRegisterDTO;
import com.connor.newsplatform.pojo.vo.AdminUserVO;
import com.connor.newsplatform.pojo.vo.LoginVO;
import com.connor.newsplatform.pojo.vo.UserVO;

public interface AuthService {
    LoginVO adminLogin(LoginDTO dto);

    AdminUserVO currentAdmin();

    void updateAdminStatus(Long id, Integer status);

    void register(UserRegisterDTO dto);

    LoginVO userLogin(LoginDTO dto);

    UserVO currentUser();

    void updateUser(UserProfileDTO dto);
}
