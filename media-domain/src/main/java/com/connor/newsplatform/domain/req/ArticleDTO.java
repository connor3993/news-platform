package com.connor.newsplatform.domain.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ArticleDTO {
    private Long id;
    @NotBlank(message = "标题不能为空")
    private String title;
    private String summary;
    private String coverUrl;
    @NotNull(message = "分类不能为空")
    private Long categoryId;
    @NotBlank(message = "正文不能为空")
    private String content;
    /** 0=免费 1=付费 */
    private Integer isPaid = 0;
    /** 付费价格（isPaid=1 时必填） */
    private BigDecimal price;
    /** 免费预览内容（付费文章使用，正文自动忽略） */
    private String paidPreview;
    /** 付费部分内容（付费文章使用，正文自动忽略） */
    private String paidOnly;
}
