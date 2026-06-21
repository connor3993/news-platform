package com.connor.newsplatform.server.service.impl;

import org.springframework.beans.BeanUtils;

final class BeanCopy {
    private BeanCopy() {
    }

    static <T> T to(Object source, Class<T> targetClass) {
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Bean copy failed", e);
        }
    }
}
