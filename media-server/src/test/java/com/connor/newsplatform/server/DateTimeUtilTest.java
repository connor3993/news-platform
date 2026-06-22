package com.connor.newsplatform.server;

import com.connor.newsplatform.common.utils.DateTimeUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilTest {

    @Test
    void formatNormalDateTime() {
        LocalDateTime time = LocalDateTime.of(2026, 6, 22, 14, 30, 45);
        assertEquals("2026-06-22 14:30:45", DateTimeUtil.format(time));
    }

    @Test
    void formatNullReturnsNull() {
        assertNull(DateTimeUtil.format(null));
    }

    @Test
    void formatMidnight() {
        LocalDateTime midnight = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        assertEquals("2026-01-01 00:00:00", DateTimeUtil.format(midnight));
    }

    @Test
    void formatEndOfDay() {
        LocalDateTime endOfDay = LocalDateTime.of(2026, 12, 31, 23, 59, 59);
        assertEquals("2026-12-31 23:59:59", DateTimeUtil.format(endOfDay));
    }
}
