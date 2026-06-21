package com.connor.newsplatform.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
}
