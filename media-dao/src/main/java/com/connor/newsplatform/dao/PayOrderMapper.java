package com.connor.newsplatform.dao;

import com.connor.newsplatform.domain.po.PayOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 付费订单 Mapper。
 * XML：media-web/src/main/resources/mapper/PayOrderMapper.xml
 */
@Mapper
public interface PayOrderMapper {
    /** 插入订单 */
    void insert(PayOrder order);

    /** 更新支付链接和状态（创建预支付单后调用） */
    void updatePayInfo(PayOrder order);

    /** 更新支付成功状态 */
    void updatePaySuccess(@Param("orderId") String orderId, @Param("payTime") LocalDateTime payTime, @Param("tradeNo") String tradeNo);

    /** 更新为关单 */
    void updateClose(@Param("orderId") String orderId);

    /** 根据订单号查 */
    PayOrder getByOrderId(@Param("orderId") String orderId);

    /** 查用户对某文章是否存在已支付订单（用于解锁判断） */
    PayOrder getPaidByUserArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);

    /** 查用户+文章的最新一笔未关单订单（用于复用未支付单） */
    PayOrder getLatestUnpaid(@Param("userId") Long userId, @Param("articleId") Long articleId);

    /** 待关单扫描：状态=1 且 下单时间在 N 分钟之前 */
    List<String> queryTimeoutCloseList(@Param("timeoutMinutes") int timeoutMinutes, @Param("limit") int limit);

    /** 漏回调扫描：等待支付超过 1 分钟的订单 */
    List<String> queryNoPayNotifyOrderList(@Param("limit") int limit);
}
