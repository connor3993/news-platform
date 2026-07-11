package com.connor.newsplatform.domain.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建支付订单请求。
 * 字段命名与 s-pay-mall-moni 的 ShopCartReq 对齐，userId 从 JWT 上下文取，不传。
 */
@Data
public class CreatePayReq implements Serializable {
    /** 文章ID */
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
}
