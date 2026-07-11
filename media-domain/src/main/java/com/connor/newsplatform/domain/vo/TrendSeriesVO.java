package com.connor.newsplatform.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 趋势数据序列：用于图表直接消费（x 轴日期、y 轴数值）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendSeriesVO {
    private List<String> dates;
    private List<Long> values;
}
