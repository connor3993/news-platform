package com.connor.newsplatform.server;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 热点分计算公式单元测试
 * hotScore = viewCount + likeCount * 4 + commentCount * 3 + favoriteCount * 3 - dislikeCount + recency * 8
 * recency = max(0, 240 - hoursSincePublish) / 10
 */
class HotScoreCalculationTest {

    @Test
    void brandNewArticleWithZeroInteractions() {
        BigDecimal score = calcHotScore(0, 0, 0, 0, 0, LocalDateTime.now());
        // recency = max(0, 240 - ~0) / 10 = 24.0
        // score = 0 + 0 + 0 + 0 - 0 + 24.0 * 8 = 192
        assertTrue(score.compareTo(BigDecimal.valueOf(190)) > 0);
    }

    @Test
    void articleWithHighLikes() {
        // 100 views, 50 likes, 10 comments, 20 favorites, 5 dislikes, published 1 hour ago
        BigDecimal score = calcHotScore(100, 50, 10, 20, 5, LocalDateTime.now().minusHours(1));
        // recency = max(0, 240 - 1) / 10 = 23.9
        // score = 100 + 50*4 + 10*3 + 20*3 - 5 + 23.9*8
        //       = 100 + 200 + 30 + 60 - 5 + 191.2 = 576.2
        assertTrue(score.compareTo(BigDecimal.valueOf(575)) > 0);
        assertTrue(score.compareTo(BigDecimal.valueOf(578)) < 0);
    }

    @Test
    void oldArticleDecaysRecency() {
        // published 300 hours ago (> 240h), recency should be 0
        BigDecimal score = calcHotScore(1000, 100, 50, 30, 10, LocalDateTime.now().minusHours(300));
        // recency = max(0, 240 - 300) / 10 = 0
        // score = 1000 + 100*4 + 50*3 + 30*3 - 10 + 0
        //       = 1000 + 400 + 150 + 90 - 10 = 1630
        assertEquals(new BigDecimal("1630.00"), score.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void dislikesReduceScore() {
        BigDecimal withDislike = calcHotScore(0, 0, 0, 0, 10, LocalDateTime.now().minusHours(240));
        BigDecimal withoutDislike = calcHotScore(0, 0, 0, 0, 0, LocalDateTime.now().minusHours(240));
        // difference should be exactly 10 (10 dislikes)
        assertEquals(0, withoutDislike.subtract(withDislike).compareTo(BigDecimal.valueOf(10)));
    }

    @Test
    void nullPublishTimeUses720Hours() {
        BigDecimal score = calcHotScore(100, 10, 5, 5, 2, null);
        // hours = 720, recency = max(0, 240 - 720) / 10 = 0
        // score = 100 + 10*4 + 5*3 + 5*3 - 2 + 0 = 100 + 40 + 15 + 15 - 2 = 168
        assertEquals(new BigDecimal("168.00"), score.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * 复刻 ArticleServiceImpl.recalculateHotScore 的计算逻辑
     */
    private BigDecimal calcHotScore(long viewCount, long likeCount, long commentCount,
                                       long favoriteCount, long dislikeCount, LocalDateTime publishTime) {
        LocalDateTime now = LocalDateTime.now();
        long hours = publishTime == null ? 720 : Math.max(1, ChronoUnit.HOURS.between(publishTime, now));
        BigDecimal recency = BigDecimal.valueOf(Math.max(0, 240 - hours))
                .divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(viewCount)
                .add(BigDecimal.valueOf(likeCount).multiply(BigDecimal.valueOf(4)))
                .add(BigDecimal.valueOf(commentCount).multiply(BigDecimal.valueOf(3)))
                .add(BigDecimal.valueOf(favoriteCount).multiply(BigDecimal.valueOf(3)))
                .subtract(BigDecimal.valueOf(dislikeCount))
                .add(recency.multiply(BigDecimal.valueOf(8)));
    }
}
