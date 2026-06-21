package com.connor.newsplatform.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.connor.newsplatform.common.constant.JwtClaimsConstant;
import com.connor.newsplatform.common.context.BaseContext;
import com.connor.newsplatform.common.exception.BusinessException;
import com.connor.newsplatform.common.properties.JwtProperties;
import com.connor.newsplatform.common.utils.JwtUtil;
import com.connor.newsplatform.pojo.dto.LoginDTO;
import com.connor.newsplatform.pojo.dto.UserProfileDTO;
import com.connor.newsplatform.pojo.dto.UserRegisterDTO;
import com.connor.newsplatform.pojo.entity.AdminUser;
import com.connor.newsplatform.pojo.entity.AppUser;
import com.connor.newsplatform.pojo.vo.AdminUserVO;
import com.connor.newsplatform.pojo.vo.LoginVO;
import com.connor.newsplatform.pojo.vo.UserVO;
import com.connor.newsplatform.server.mapper.AdminUserMapper;
import com.connor.newsplatform.server.mapper.AppUserMapper;
import com.connor.newsplatform.server.service.AuthService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    private final AdminUserMapper adminUserMapper;
    private final AppUserMapper appUserMapper;
    private final JwtProperties jwtProperties;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(AdminUserMapper adminUserMapper, AppUserMapper appUserMapper, JwtProperties jwtProperties) {
        this.adminUserMapper = adminUserMapper;
        this.appUserMapper = appUserMapper;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public LoginVO adminLogin(LoginDTO dto) {
        AdminUser user = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, dto.getUsername()));
        if (user == null || !passwordMatches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException("账号已禁用");
        }
        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setName(user.getName());
        vo.setRole(user.getRole());
        vo.setToken(JwtUtil.createJwt(jwtProperties.getAdminSecretKey(), jwtProperties.getTtl(),
                claims(user.getId(), JwtClaimsConstant.ADMIN, user.getUsername())));
        return vo;
    }

    @Override
    public AdminUserVO currentAdmin() {
        AdminUser user = adminUserMapper.selectById(BaseContext.getCurrentId());
        if (user == null) {
            throw new BusinessException("管理员不存在");
        }
        return BeanCopy.to(user, AdminUserVO.class);
    }

    @Override
    public void updateAdminStatus(Long id, Integer status) {
        AdminUser user = new AdminUser();
        user.setId(id);
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateUser(BaseContext.getCurrentId());
        adminUserMapper.updateById(user);
    }

    @Override
    @Transactional
    public void register(UserRegisterDTO dto) {
        Long count = appUserMapper.selectCount(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        AppUser user = new AppUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() == null || dto.getNickname().isBlank() ? dto.getUsername() : dto.getNickname());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        appUserMapper.insert(user);
    }

    @Override
    public LoginVO userLogin(LoginDTO dto) {
        AppUser user = appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getUsername, dto.getUsername()));
        if (user == null || !passwordMatches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException("账号已禁用");
        }
        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setToken(JwtUtil.createJwt(jwtProperties.getUserSecretKey(), jwtProperties.getTtl(),
                claims(user.getId(), JwtClaimsConstant.USER, user.getUsername())));
        return vo;
    }

    @Override
    public UserVO currentUser() {
        AppUser user = appUserMapper.selectById(BaseContext.getCurrentId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return BeanCopy.to(user, UserVO.class);
    }

    @Override
    public void updateUser(UserProfileDTO dto) {
        AppUser user = new AppUser();
        user.setId(BaseContext.getCurrentId());
        user.setNickname(dto.getNickname());
        user.setAvatar(dto.getAvatar());
        user.setUpdateTime(LocalDateTime.now());
        appUserMapper.updateById(user);
    }

    private boolean passwordMatches(String raw, String encoded) {
        if (encoded == null) {
            return false;
        }
        if (encoded.startsWith("$2a$") || encoded.startsWith("$2b$") || encoded.startsWith("$2y$")) {
            return passwordEncoder.matches(raw, encoded);
        }
        return raw.equals(encoded);
    }

    private Map<String, Object> claims(Long id, String userType, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, id);
        claims.put(JwtClaimsConstant.USER_TYPE, userType);
        claims.put(JwtClaimsConstant.USERNAME, username);
        return claims;
    }
}
