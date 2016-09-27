package com.taobao.profile.test;

import org.junit.Assert;
import org.junit.Test;

import com.taobao.profile.utils.Utilities;
import com.taobao.profile.utils.VariableNotFoundException;

public class UtilitiesTest {

    @Test
    public void testReplaceVariables() throws VariableNotFoundException {
        String source = "${user.home}/logs/${user.language}/tprofiler.log";
        String str1 = Utilities.replaceVariables(source, System.getProperties());
        String str2 = System.getProperty("user.home") + "/logs/" + System.getProperty(
                "user.language") + "/tprofiler.log";
        Assert.assertEquals(str1, str2);
    }
}
