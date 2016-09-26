package com.taobao.profile.test;

import org.junit.Assert;
import org.junit.Test;

import com.taobao.profile.config.ProfFilter;

/**
 * @author zhoulifu
 */
public class ProFilterTest {
    @Test
    public void testNeedTransformCheck(){
        ProfFilter.addIncludeClass("foo");
        ProfFilter.addExcludeClass("foo.bar");
//        ProfFilter.addIncludeClass("java.util");

        Assert.assertTrue(ProfFilter.needsTransform(null, "foo.baz"));
        Assert.assertFalse(ProfFilter.needsTransform(null, "foo.bar"));

        // default setting
        Assert.assertFalse(ProfFilter.needsTransform(null, "java"));
        Assert.assertTrue(ProfFilter.needsTransform(null, "java.util"));
    }
}
