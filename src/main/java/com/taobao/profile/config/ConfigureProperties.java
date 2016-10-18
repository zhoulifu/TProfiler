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

    @Override
    public String getProperty(String key) {
        String value = super.getProperty(key);
        if (value == null) {
            throw new VariableNotFoundException("property " + key + " not found");
        }

        return parsePlaceHolder(value.trim());
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        try {
            return this.getProperty(key);
        } catch (VariableNotFoundException e) {
            return defaultValue;
        }
    }

    final String parsePlaceHolder(String source) throws VariableNotFoundException {
        //从后向前查找
        int rbi = source.lastIndexOf("}");
        if (rbi == -1) {
            return source;
        }

        StringBuilder sb = new StringBuilder(source);
        while (rbi != -1) {
            int lbi = sb.lastIndexOf("${");
            if (lbi == -1) {
                throw new VariableNotFoundException("invalid token " + source);
            }

            String key = sb.substring(lbi + 2, rbi); //skip '${'
            String v = super.getProperty(key);
            if (v == null) {
                v = context.getProperty(key);
            }

            if (v == null) {
                throw new VariableNotFoundException(
                        "parse " + source + " failed, cause variable ${" + key +
                        "} not found");
            }

            sb.delete(lbi, rbi + 1);
            sb.insert(lbi, v);
            rbi = sb.lastIndexOf("}");
        }

        return sb.toString().trim();
    }
}
