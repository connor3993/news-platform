package com.connor.newsplatform.web.controller.user;

import com.connor.newsplatform.common.annotation.LogRecord;
import com.connor.newsplatform.common.result.Result;
import com.connor.newsplatform.domain.req.UserProfileDTO;
import com.connor.newsplatform.domain.vo.UserVO;
import com.connor.newsplatform.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/user")
public class UserProfileController {
    private final AuthService authService;

    public UserProfileController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/info")
    public Result<UserVO> info() {
        return Result.success(authService.currentUser());
    }

    @LogRecord("修改用户资料")
    @PutMapping("/info")
    public Result<Void> update(@RequestBody UserProfileDTO dto) {
        authService.updateUser(dto);
        return Result.success();
    }
}
