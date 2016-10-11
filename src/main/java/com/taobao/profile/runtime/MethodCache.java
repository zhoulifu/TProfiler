/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile.runtime;

import java.util.ArrayList;
import java.util.List;

import com.taobao.profile.Manager;
import com.taobao.profile.Profiler;
import com.taobao.profile.utils.DailyRollingFileWriter;

public class MethodCache {
    private static final int INIT_CACHE_SIZE = 10240;
    private static List<MethodInfo> methodInfoCache = new ArrayList<>(INIT_CACHE_SIZE);
    private static DailyRollingFileWriter fileWriter = new DailyRollingFileWriter(
            Manager.METHOD_LOG_PATH);

    /**
     * Request a slot to cache the method information and return its id.
     *
     * @return method's id
     */
    public synchronized static int Request(String className, String methodName) {
        methodInfoCache.add(new MethodInfo(className, methodName));
        return methodInfoCache.size() - 1;
    }

    /**
     * @param id method id
     * @param lineNum line number
     */
    public static void UpdateLineNum(int id, int lineNum) {
        methodInfoCache.get(id).setLineNum(lineNum);
    }

    public synchronized static void flushMethodData() {
        String header = "instrument_class_count:" + Profiler.getClassCount() +
                        " instrument_method_count:" + Profiler.getMethodCount() + "\n";
        fileWriter.append(header);

        List<MethodInfo> methods = methodInfoCache;
        int index = 0;
        StringBuilder sb = new StringBuilder();
        for (MethodInfo method : methods) {
            sb.append(index).append(' ').append(method.toString()).append('\n');
            fileWriter.append(sb.toString());
            sb.setLength(0);
            if ((index & 63) == 0) {
                fileWriter.flushAppend();
            }
            index++;
        }

        fileWriter.flushAppend();
    }
}
