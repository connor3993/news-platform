package com.connor.newsplatform.common.aspect;

import com.connor.newsplatform.common.annotation.AutoFill;
import com.connor.newsplatform.common.context.BaseContext;
import com.connor.newsplatform.common.enumeration.OperationType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class AutoFillAspect {
    @Pointcut("@annotation(com.connor.newsplatform.common.annotation.AutoFill)")
    public void autoFillPointcut() {
    }

    @Before("autoFillPointcut() && @annotation(autoFill)")
    public void fill(JoinPoint joinPoint, AutoFill autoFill) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0 || args[0] == null) {
            return;
        }
        Object entity = args[0];
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        if (autoFill.value() == OperationType.INSERT) {
            invoke(entity, "setCreateTime", LocalDateTime.class, now);
            invoke(entity, "setUpdateTime", LocalDateTime.class, now);
            invoke(entity, "setCreateUser", Long.class, currentId);
            invoke(entity, "setUpdateUser", Long.class, currentId);
        } else {
            invoke(entity, "setUpdateTime", LocalDateTime.class, now);
            invoke(entity, "setUpdateUser", Long.class, currentId);
        }
    }

    private void invoke(Object target, String methodName, Class<?> parameterType, Object value) {
        if (value == null && parameterType == Long.class) {
            return;
        }
        try {
            Method method = target.getClass().getMethod(methodName, parameterType);
            method.invoke(target, value);
        } catch (ReflectiveOperationException ignored) {
            // Entity does not define the public field setter; skip quietly.
        }
    }
}
