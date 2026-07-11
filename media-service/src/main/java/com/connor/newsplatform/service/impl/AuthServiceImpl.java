package com.connor.newsplatform.service.impl;

import com.connor.newsplatform.common.constant.JwtClaimsConstant;
import com.connor.newsplatform.common.context.BaseContext;
import com.connor.newsplatform.common.exception.BusinessException;
import com.connor.newsplatform.common.properties.JwtProperties;
import com.connor.newsplatform.common.utils.JwtUtil;
import com.connor.newsplatform.domain.req.LoginDTO;
import com.connor.newsplatform.domain.req.UserProfileDTO;
import com.connor.newsplatform.domain.req.UserRegisterDTO;
import com.connor.newsplatform.domain.po.SysUser;
import com.connor.newsplatform.domain.vo.AdminUserVO;
import com.connor.newsplatform.domain.vo.LoginVO;
import com.connor.newsplatform.domain.vo.UserVO;
import com.connor.newsplatform.dao.SysUserMapper;
import com.connor.newsplatform.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务（admin / user 统一到 sys_user 表，借助 user_type 区分）
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final JwtProperties jwtProperties;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(SysUserMapper sysUserMapper, JwtProperties jwtProperties) {
        this.sysUserMapper = sysUserMapper;
        this.jwtProperties = jwtProperties;
    }

    /**
     * 管理员登录
     */
    @Override
    public LoginVO adminLogin(LoginDTO dto) {
        SysUser user = sysUserMapper.getByUsernameAndType(dto.getUsername(), JwtClaimsConstant.ADMIN);
        checkPassword(dto.getPassword(), user == null ? null : user.getPassword());
        checkAccountStatus(user.getStatus());

        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setName(user.getName());
        vo.setToken(createToken(user.getId(), JwtClaimsConstant.ADMIN, user.getUsername()));
        return vo;
    }

    /**
     * 用户登录
     */
    @Override
    public LoginVO userLogin(LoginDTO dto) {
        SysUser user = sysUserMapper.getByUsernameAndType(dto.getUsername(), JwtClaimsConstant.USER);
        checkPassword(dto.getPassword(), user == null ? null : user.getPassword());
        checkAccountStatus(user.getStatus());

        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setToken(createToken(user.getId(), JwtClaimsConstant.USER, user.getUsername()));
        return vo;
    }

    /**
     * 验证密码（仅支持 BCrypt 加密密码）
     */
    private void checkPassword(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || !passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BusinessException("用户名或密码错误");
        }
    }

    /**
     * 检查账号启用状态
     */
    private void checkAccountStatus(Integer status) {
        if (status == null || status == 0) {
            throw new BusinessException("账号已禁用");
        }
    }

    /**
     * 获取当前管理员信息
     */
    @Override
    public AdminUserVO currentAdmin() {
        SysUser user = sysUserMapper.getById(BaseContext.getCurrentId());
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
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateUser(BaseContext.getCurrentId());
        sysUserMapper.update(user);
    }

    /**
     * 用户注册（密码使用 BCrypt 加密存储）
     */
    @Override
    @Transactional
    public void register(UserRegisterDTO dto) {
        Long count = sysUserMapper.countByUsername(dto.getUsername());
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() == null || dto.getNickname().isBlank()
                ? dto.getUsername() : dto.getNickname());
        user.setUserType(JwtClaimsConstant.USER);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.insert(user);
    }

    /**
     * 获取当前用户信息
     */
    @Override
    public UserVO currentUser() {
        SysUser user = sysUserMapper.getById(BaseContext.getCurrentId());
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
        SysUser user = new SysUser();
        user.setId(BaseContext.getCurrentId());
        user.setNickname(dto.getNickname());
        user.setAvatar(dto.getAvatar());
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.update(user);
    }

    /**
     * 生成 JWT Token
     */
    private String createToken(Long id, String userType, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, id);
        claims.put(JwtClaimsConstant.USER_TYPE, userType);
        claims.put(JwtClaimsConstant.USERNAME, username);
        return JwtUtil.createJwt(jwtProperties.getSecretKey(), jwtProperties.getTtl(), claims);
    }
}
