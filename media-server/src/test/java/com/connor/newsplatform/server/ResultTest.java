package com.connor.newsplatform.server;

import com.connor.newsplatform.common.result.Result;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void successWithoutData() {
        Result<Object> result = Result.success();
        assertEquals(1, result.getCode());
        assertEquals("success", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void successWithData() {
        Result<String> result = Result.success("hello");
        assertEquals(1, result.getCode());
        assertEquals("success", result.getMsg());
        assertEquals("hello", result.getData());
    }

    @Test
    void successWithListData() {
        List<Integer> data = List.of(1, 2, 3);
        Result<List<Integer>> result = Result.success(data);
        assertEquals(1, result.getCode());
        assertEquals(3, result.getData().size());
    }

    @Test
    void error() {
        Result<Object> result = Result.error("参数错误");
        assertEquals(0, result.getCode());
        assertEquals("参数错误", result.getMsg());
        assertNull(result.getData());
    }
}
