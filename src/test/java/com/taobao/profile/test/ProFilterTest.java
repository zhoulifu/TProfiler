package com.taobao.profile.test;

import org.junit.Assert;
import org.junit.Test;

import com.taobao.profile.config.FilterStrategy;
import com.taobao.profile.config.ProfFilter;

/**
 * @author zhoulifu
 */
public class ProFilterTest {
    @Test
    public void testNeedTransformCheck() {
        ProfFilter.addIncludeClass("foo.bar.quz");
        ProfFilter.addExcludeClass("foo.baz");

        Assert.assertTrue(ProfFilter.needsTransform(null, "foo.bar.quz"));
        Assert.assertFalse(ProfFilter.needsTransform(null, "foo.baz"));

        // default setting
        Assert.assertFalse(ProfFilter.needsTransform(null, "java"));

        // non-configured depends on the default filter strategy
        Assert.assertEquals(ProfFilter.needsTransform(null, "java.util"),
                            ProfFilter.DEFAULT_STRATEGY == FilterStrategy.INCLUDE);
        Assert.assertEquals(ProfFilter.needsTransform(null, "foo.bar"),
                            ProfFilter.DEFAULT_STRATEGY == FilterStrategy.INCLUDE);
    }
}
