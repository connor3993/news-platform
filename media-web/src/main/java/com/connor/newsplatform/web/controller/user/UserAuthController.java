package com.connor.newsplatform.web.controller.user;

import com.connor.newsplatform.common.result.Result;
import com.connor.newsplatform.domain.req.LoginDTO;
import com.connor.newsplatform.domain.req.UserRegisterDTO;
import com.connor.newsplatform.domain.vo.LoginVO;
import com.connor.newsplatform.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/auth")
public class UserAuthController {
    private final AuthService authService;

    public UserAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.userLogin(dto));
    }
}
