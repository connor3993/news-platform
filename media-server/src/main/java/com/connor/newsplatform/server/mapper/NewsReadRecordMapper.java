package com.connor.newsplatform.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.connor.newsplatform.pojo.entity.NewsReadRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NewsReadRecordMapper extends BaseMapper<NewsReadRecord> {
}
