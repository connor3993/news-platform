package com.connor.newsplatform.common.context;

public final class BaseContext {
    private static final ThreadLocal<Long> CURRENT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_TYPE = new ThreadLocal<>();

    private BaseContext() {
    }

    public static void setCurrentId(Long id) {
        CURRENT_ID.set(id);
    }

    public static Long getCurrentId() {
        return CURRENT_ID.get();
    }

    public static void setCurrentType(String type) {
        CURRENT_TYPE.set(type);
    }

    public static String getCurrentType() {
        return CURRENT_TYPE.get();
    }

    public static void removeCurrent() {
        CURRENT_ID.remove();
        CURRENT_TYPE.remove();
    }
}
