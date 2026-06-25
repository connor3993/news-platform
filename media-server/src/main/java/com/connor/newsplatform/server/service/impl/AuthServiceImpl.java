package com.connor.newsplatform.server.service.impl;

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
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务
 */
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

    /**
     * 管理员登录
     */
    @Override
    public LoginVO adminLogin(LoginDTO dto) {
        // 1. 查询管理员
        AdminUser user = adminUserMapper.getByUsername(dto.getUsername());
        if (user == null || !passwordMatches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        // 2. 检查账号状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException("账号已禁用");
        }
        // 3. 构造返回数据
        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setName(user.getName());
        vo.setRole(user.getRole());
        vo.setToken(createToken(user.getId(), JwtClaimsConstant.ADMIN, user.getUsername()));
        return vo;
    }

    /**
     * 获取当前管理员信息
     */
    @Override
    public AdminUserVO currentAdmin() {
        AdminUser user = adminUserMapper.getById(BaseContext.getCurrentId());
        if (user == null) {
            throw new BusinessException("管理员不存在");
        }
        AdminUserVO vo = new AdminUserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /**
     * 启用禁用管理员账号
     */
    @Override
    public void updateAdminStatus(Long id, Integer status) {
        AdminUser user = new AdminUser();
        user.setId(id);
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateUser(BaseContext.getCurrentId());
        adminUserMapper.update(user);
    }

    /**
     * 用户注册
     */
    @Override
    @Transactional
    public void register(UserRegisterDTO dto) {
        // 1. 检查用户名是否已存在
        Long count = appUserMapper.countByUsername(dto.getUsername());
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        // 2. 新增用户
        AppUser user = new AppUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() == null || dto.getNickname().isBlank()
                ? dto.getUsername() : dto.getNickname());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        appUserMapper.insert(user);
    }

    /**
     * 用户登录
     */
    @Override
    public LoginVO userLogin(LoginDTO dto) {
        // 1. 查询用户
        AppUser user = appUserMapper.getByUsername(dto.getUsername());
        if (user == null || !passwordMatches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        // 2. 检查账号状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException("账号已禁用");
        }
        // 3. 构造返回数据
        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setToken(createToken(user.getId(), JwtClaimsConstant.USER, user.getUsername()));
        return vo;
    }

    /**
     * 获取当前用户信息
     */
    @Override
    public UserVO currentUser() {
        AppUser user = appUserMapper.getById(BaseContext.getCurrentId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /**
     * 修改用户资料
     */
    @Override
    public void updateUser(UserProfileDTO dto) {
        AppUser user = new AppUser();
        user.setId(BaseContext.getCurrentId());
        user.setNickname(dto.getNickname());
        user.setAvatar(dto.getAvatar());
        user.setUpdateTime(LocalDateTime.now());
        appUserMapper.update(user);
    }

    /**
     * 密码校验：BCrypt加密的用BCrypt比对，明文直接比对
     */
    private boolean passwordMatches(String raw, String encoded) {
        if (encoded == null) {
            return false;
        }
        if (encoded.startsWith("$2a$") || encoded.startsWith("$2b$") || encoded.startsWith("$2y$")) {
            return passwordEncoder.matches(raw, encoded);
        }
        return raw.equals(encoded);
    }

    /**
     * 生成JWT Token
     */
    private String createToken(Long id, String userType, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, id);
        claims.put(JwtClaimsConstant.USER_TYPE, userType);
        claims.put(JwtClaimsConstant.USERNAME, username);
        String secret = JwtClaimsConstant.ADMIN.equals(userType)
                ? jwtProperties.getAdminSecretKey() : jwtProperties.getUserSecretKey();
        return JwtUtil.createJwt(secret, jwtProperties.getTtl(), claims);
    }
}
