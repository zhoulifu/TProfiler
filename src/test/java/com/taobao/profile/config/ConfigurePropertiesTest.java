package com.taobao.profile.config;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.taobao.profile.utils.VariableNotFoundException;

public class ConfigurePropertiesTest {
    private ConfigureProperties properties;

    @Before
    public void before() {
        properties = new ConfigureProperties();
    }

    @Test
    public void testConfigureProperties() {
        properties.put("file.name", "tprofiler.log");
        properties.put("log.file.path", "${user.home}/${file.name}");

        Assert.assertEquals(properties.getProperty("log.file.path"),
                            System.getProperty("user.home") + "/tprofiler.log");
    }

    @Test
    public void testConfigure() throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream(
                "profile.properties");
        properties.load(in);

        try {
            String logFilePath = properties.getProperty("logFilePath");
            Assert.assertEquals(logFilePath,
                                System.getProperty("user.home") + "/logs/tprofiler.log");
        } finally {
            in.close();
        }
    }

    @Test
    @SuppressWarnings("all")
    public void testReplaceVariables() throws VariableNotFoundException {
        String source = "${user.home}/logs/${user.language}/tprofiler.log";
        String str1 = properties.parsePlaceHolder(source);
        String str2 = System.getProperty("user.home") + "/logs/" + System.getProperty(
                "user.language") + "/tprofiler.log";
        Assert.assertEquals(str1, str2);
    }

    @Test
    public void testDefaultValue() {
        Assert.assertEquals("default", properties.getProperty("foo", "default"));

        properties.setProperty("foo", "${bar}");

        Assert.assertEquals("default", properties.getProperty("foo", "default"));
    }
}
