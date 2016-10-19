package com.taobao.profile.utils;

import static com.taobao.profile.utils.StringUtils.upperCaseFirstLetter;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilsTest {
    @Test
    public void testUpperCaseFirstLetter() {
        assertEquals(upperCaseFirstLetter("hello"), "Hello");
        assertEquals(upperCaseFirstLetter("Hello"), "Hello");
        assertEquals(upperCaseFirstLetter("_hello"), "_hello");
    }
}
