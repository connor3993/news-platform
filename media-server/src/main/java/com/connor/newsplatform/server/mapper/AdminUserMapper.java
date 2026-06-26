package com.connor.newsplatform.server.mapper;

import com.connor.newsplatform.pojo.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 管理员用户 Mapper
 */
@Mapper
public interface AdminUserMapper {

    /**
     * 根据用户名查询管理员
     */
    @Select("select * from admin_user where username = #{username}")
    AdminUser getByUsername(@Param("username") String username);

    /**
     * 根据id查询管理员
     */
    @Select("select * from admin_user where id = #{id}")
    AdminUser getById(@Param("id") Long id);

    /**
     * 新增管理员
     */
    void insert(AdminUser adminUser);

    /**
     * 修改管理员信息（动态SQL，只更新非空字段）
     */
    void update(AdminUser adminUser);
}
