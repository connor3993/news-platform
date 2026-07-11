package com.connor.newsplatform.service;

import com.connor.newsplatform.domain.req.LoginDTO;
import com.connor.newsplatform.domain.req.UserProfileDTO;
import com.connor.newsplatform.domain.req.UserRegisterDTO;
import com.connor.newsplatform.domain.vo.AdminUserVO;
import com.connor.newsplatform.domain.vo.LoginVO;
import com.connor.newsplatform.domain.vo.UserVO;

public interface AuthService {
    LoginVO adminLogin(LoginDTO dto);

    AdminUserVO currentAdmin();

    void updateAdminStatus(Long id, Integer status);

    void register(UserRegisterDTO dto);

    LoginVO userLogin(LoginDTO dto);

    UserVO currentUser();

    void updateUser(UserProfileDTO dto);
}
