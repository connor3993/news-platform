package com.connor.newsplatform.server;

import com.connor.newsplatform.common.context.BaseContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaseContextTest {

    @AfterEach
    void cleanup() {
        BaseContext.removeCurrent();
    }

    @Test
    void setAndGetCurrentId() {
        assertNull(BaseContext.getCurrentId());
        BaseContext.setCurrentId(123L);
        assertEquals(123L, BaseContext.getCurrentId());
    }

    @Test
    void setAndGetCurrentType() {
        assertNull(BaseContext.getCurrentType());
        BaseContext.setCurrentType("admin");
        assertEquals("admin", BaseContext.getCurrentType());
    }

    @Test
    void removeCurrentClearsBoth() {
        BaseContext.setCurrentId(999L);
        BaseContext.setCurrentType("user");
        BaseContext.removeCurrent();
        assertNull(BaseContext.getCurrentId());
        assertNull(BaseContext.getCurrentType());
    }

    @Test
    void threadLocalIsolation() throws InterruptedException {
        BaseContext.setCurrentId(1L);
        BaseContext.setCurrentType("admin");

        final long[] otherThreadId = new long[1];
        final String[] otherThreadType = new String[1];

        Thread other = new Thread(() -> {
            otherThreadId[0] = BaseContext.getCurrentId() == null ? 0 : BaseContext.getCurrentId();
            otherThreadType[0] = BaseContext.getCurrentType();
        });
        other.start();
        other.join();

        assertEquals(1L, BaseContext.getCurrentId());
        assertEquals("admin", BaseContext.getCurrentType());
        assertEquals(0L, otherThreadId[0]);
        assertNull(otherThreadType[0]);
    }
}
