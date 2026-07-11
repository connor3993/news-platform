package com.connor.newsplatform.dao;

import com.connor.newsplatform.domain.po.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 系统用户 Mapper（合并原 admin_user / app_user）
 */
@Mapper
public interface SysUserMapper {

    /**
     * 根据用户名 + 用户类型查询
     */
    @Select("select * from sys_user where username = #{username} and user_type = #{userType}")
    SysUser getByUsernameAndType(@Param("username") String username, @Param("userType") String userType);

    /**
     * 根据id查询
     */
    @Select("select * from sys_user where id = #{id}")
    SysUser getById(@Param("id") Long id);

    /**
     * 根据微信 openid 查询（扫码登录绑定）
     */
    @Select("select * from sys_user where openid = #{openid}")
    SysUser getByOpenid(@Param("openid") String openid);

    /**
     * 查询用户名是否已存在（跨用户类型）
     */
    @Select("select count(*) from sys_user where username = #{username}")
    Long countByUsername(@Param("username") String username);

    /**
     * 新增用户
     */
    void insert(SysUser user);

    /**
     * 修改用户信息（动态SQL，只更新非空字段）
     */
    void update(SysUser user);
}
