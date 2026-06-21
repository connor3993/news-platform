package com.connor.newsplatform.server.controller.admin;

import com.connor.newsplatform.common.annotation.LogRecord;
import com.connor.newsplatform.common.result.Result;
import com.connor.newsplatform.pojo.dto.LoginDTO;
import com.connor.newsplatform.pojo.vo.AdminUserVO;
import com.connor.newsplatform.pojo.vo.LoginVO;
import com.connor.newsplatform.server.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {
    private final AuthService authService;

    public AdminAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.adminLogin(dto));
    }

    @LogRecord("管理员退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @GetMapping("/info")
    public Result<AdminUserVO> info() {
        return Result.success(authService.currentAdmin());
    }

    @LogRecord("管理员账号启用禁用")
    @PostMapping("/status/{status}")
    public Result<Void> status(@PathVariable Integer status, @RequestParam Long id) {
        authService.updateAdminStatus(id, status);
        return Result.success();
    }
}
