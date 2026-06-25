package com.connor.newsplatform.server.mapper;

import com.connor.newsplatform.pojo.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AppUserMapper {

    /** 根据用户名查询用户 */
    @Select("select * from app_user where username = #{username}")
    AppUser getByUsername(@Param("username") String username);

    /** 根据id查询用户 */
    @Select("select * from app_user where id = #{id}")
    AppUser getById(@Param("id") Long id);

    /** 查询用户名是否已存在 */
    @Select("select count(*) from app_user where username = #{username}")
    Long countByUsername(@Param("username") String username);

    /** 新增用户 */
    void insert(AppUser appUser);

    /** 修改用户（动态SQL） */
    void update(AppUser appUser);
}
