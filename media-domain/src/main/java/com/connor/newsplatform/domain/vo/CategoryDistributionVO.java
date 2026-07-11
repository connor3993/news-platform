package com.connor.newsplatform.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDistributionVO {
    private Long categoryId;
    private String categoryName;
    private Long count;
}
