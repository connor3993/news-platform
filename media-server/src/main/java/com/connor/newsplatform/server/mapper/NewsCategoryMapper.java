package com.connor.newsplatform.server.mapper;

import com.github.pagehelper.Page;
import com.connor.newsplatform.pojo.entity.NewsCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface NewsCategoryMapper {

    /** 分页查询分类 */
    Page<NewsCategory> pageQuery(@Param("name") String name, @Param("status") Integer status);

    /** 查询所有启用的分类 */
    @Select("select * from news_category where status = 1 order by sort asc")
    List<NewsCategory> selectEnabledList();

    /** 根据id查询分类 */
    @Select("select * from news_category where id = #{id}")
    NewsCategory getById(@Param("id") Long id);

    /** 根据id列表批量查询 */
    List<NewsCategory> selectByIds(@Param("ids") List<Long> ids);

    /** 查询分类名称是否已存在（排除自身） */
    @Select("select count(*) from news_category where name = #{name} and (#{currentId} is null or id != #{currentId})")
    Long countByName(@Param("name") String name, @Param("currentId") Long currentId);

    /** 新增分类 */
    void insert(NewsCategory category);

    /** 修改分类（动态SQL） */
    void update(NewsCategory category);

    /** 根据id删除分类 */
    @Delete("delete from news_category where id = #{id}")
    void deleteById(@Param("id") Long id);
}
