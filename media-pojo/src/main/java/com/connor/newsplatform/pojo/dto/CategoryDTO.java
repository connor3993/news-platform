package com.connor.newsplatform.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    @NotBlank(message = "分类名称不能为空")
    private String name;
    private Integer sort = 0;
    private Integer status = 1;
}
