package com.connor.newsplatform.server.controller.user;

import com.connor.newsplatform.common.annotation.LogRecord;
import com.connor.newsplatform.common.result.Result;
import com.connor.newsplatform.pojo.dto.UserProfileDTO;
import com.connor.newsplatform.pojo.vo.UserVO;
import com.connor.newsplatform.server.service.AuthService;
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
