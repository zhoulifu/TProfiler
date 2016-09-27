package com.taobao.profile.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author zhoulifu
 */
public class ProFilterTest {
    @Before
    public void before() {
        ProfFilter.reset();
    }

    @Test
    public void testExcludeMost() {
        ProfFilter.addIncludeClass("foo.bar.quz");
        ProfFilter.addExcludeClass("foo.bar");
        ProfFilter.addExcludeClass("foo.baz");

        Assert.assertTrue(ProfFilter.needsTransform(null, "foo.bar.quz"));
        Assert.assertFalse(ProfFilter.needsTransform(null, "foo.baz"));
        Assert.assertFalse(ProfFilter.needsTransform(null, "foo.bar"));

        // default setting
        Assert.assertFalse(ProfFilter.needsTransform(null, "java"));

        // non-configured depends on their parent package's configuration
        Assert.assertEquals(ProfFilter.needsTransform(null, "java.util"),
                            ProfFilter.needsTransform(null, "java"));
        Assert.assertEquals(ProfFilter.needsTransform(null, "foo.bar.baz"),
                            ProfFilter.needsTransform(null, "foo.bar"));
        Assert.assertEquals(ProfFilter.needsTransform(null, "foo.quz"),
                            ProfFilter.needsTransform(null, "foo"));
        Assert.assertEquals(ProfFilter.needsTransform(null, "foo"),
                            ProfFilter.DEFAULT_STRATEGY == FilterStrategy.INCLUDE);
        Assert.assertFalse(ProfFilter.needsTransform(null, "foo.baz.quz"));
    }

    @Test
    public void testIncludeMost() {
        ProfFilter.addIncludeClass("foo");
        ProfFilter.addExcludeClass("foo.bar");

        Assert.assertTrue(ProfFilter.needsTransform(null, "foo"));
        Assert.assertFalse(ProfFilter.needsTransform(null, "foo.bar"));

        // non-configured depends on their parent package's configuration
        Assert.assertTrue(ProfFilter.needsTransform(null, "foo.quz"));
    }
}
