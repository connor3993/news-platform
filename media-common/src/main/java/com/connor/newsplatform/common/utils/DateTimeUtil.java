package com.connor.newsplatform.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtil() {
    }

    public static String format(LocalDateTime time) {
        return time == null ? null : DATE_TIME_FORMATTER.format(time);
    }
}
