package com.connor.newsplatform.server.mapper;

import com.connor.newsplatform.pojo.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * APP端用户（普通用户/自媒体）Mapper
 */
@Mapper
public interface AppUserMapper {

    /**
     * 根据用户名查询用户
     */
    @Select("select * from app_user where username = #{username}")
    AppUser getByUsername(@Param("username") String username);

    /**
     * 根据id查询用户
     */
    @Select("select * from app_user where id = #{id}")
    AppUser getById(@Param("id") Long id);

    /**
     * 查询用户名是否已存在
     */
    @Select("select count(*) from app_user where username = #{username}")
    Long countByUsername(@Param("username") String username);

    /**
     * 新增用户
     */
    void insert(AppUser appUser);

    /**
     * 修改用户信息（动态SQL，只更新非空字段）
     */
    void update(AppUser appUser);
}
