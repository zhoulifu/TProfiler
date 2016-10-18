package com.taobao.profile.config;

import java.util.Properties;

import com.taobao.profile.utils.VariableNotFoundException;

/**
 * {@link Properties} which support to refer back to previously defined values (e.g.
 * from system properties)
 *
 * @author manlge
 * @author zlf
 */
class ConfigureProperties extends Properties {
    private static final long serialVersionUID = -3868173073422671544L;

    private Properties context;

    ConfigureProperties() {
        super();
        this.context = System.getProperties();
    }

    public String getProperty(String key) {
        String value = super.getProperty(key);
        if (value == null) {
            throw new VariableNotFoundException("variable " + key + " not found");
        }

        return parsePlaceHolder(value.trim());
    }

    public String getProperty(String key, String defaultValue) {
        try {
            return this.getProperty(key);
        } catch (VariableNotFoundException e) {
            return defaultValue;
        }
    }

    final String parsePlaceHolder(String source) throws VariableNotFoundException {
        //从后向前查找
        int p = source.lastIndexOf('}');
        while (p != -1) {
            int p1 = source.lastIndexOf("${");
            if (p1 == -1) {
                return source;
            }

            String key = source.substring(p1 + 2, p); //skip '${'
            String v = super.getProperty(key);
            if (v == null) {
                v = context.getProperty(key);
            }

            if (v == null) {
                throw new VariableNotFoundException("variable " + key + " not found");
            }

            String start = source.substring(0, p1);
            String end = source.substring(p + 1);
            source = start + v + end;
            p = source.lastIndexOf('}');
        }

        return source.trim();
    }
}
