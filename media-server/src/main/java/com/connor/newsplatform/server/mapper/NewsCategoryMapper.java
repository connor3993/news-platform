package com.connor.newsplatform.server.mapper;

import com.github.pagehelper.Page;
import com.connor.newsplatform.pojo.entity.NewsCategory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 新闻分类 Mapper
 */
@Mapper
public interface NewsCategoryMapper {

    /**
     * 分页查询分类（支持名称模糊搜索和状态筛选）
     */
    Page<NewsCategory> pageQuery(@Param("name") String name, @Param("status") Integer status);

    /**
     * 查询所有启用的分类（按排序字段升序）
     */
    @Select("select * from news_category where status = 1 order by sort asc")
    List<NewsCategory> selectEnabledList();

    /**
     * 根据id查询分类详情
     */
    @Select("select * from news_category where id = #{id}")
    NewsCategory getById(@Param("id") Long id);

    /**
     * 根据id列表批量查询分类
     */
    List<NewsCategory> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 查询分类名称是否已存在（排除自身，用于编辑校验）
     */
    @Select("select count(*) from news_category where name = #{name} and (#{currentId} is null or id != #{currentId})")
    Long countByName(@Param("name") String name, @Param("currentId") Long currentId);

    /**
     * 新增分类
     */
    @Insert("insert into news_category (name, sort, status, create_time, update_time, create_user, update_user) " +
            "values (#{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(NewsCategory category);

    /**
     * 修改分类信息（动态SQL，只更新非空字段）
     */
    void update(NewsCategory category);

    /**
     * 根据id删除分类
     */
    @Delete("delete from news_category where id = #{id}")
    void deleteById(@Param("id") Long id);
}
