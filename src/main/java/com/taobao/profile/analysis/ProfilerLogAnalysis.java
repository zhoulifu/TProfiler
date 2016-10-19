/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.taobao.profile.MethodFrame;
import com.taobao.profile.utils.MathUtils;

/**
 * 分析Profiler生成的Log
 *
 * @author shutong.dy
 * @since 2012-1-11
 */
public class ProfilerLogAnalysis {

    private String logPath;
    private String methodPath;
    private boolean nano = false;
    private long currentThreadId = -1;
    private List<MethodFrame> threadList = new ArrayList<>();
    private Map<Long, MethodElapsed> cacheMethodMap = new HashMap<>();
    private Map<Long, String> methodIdMap = new HashMap<>();

    /**
     * @param inPath path of 'tprofiler.log'
     * @param methodPath path of 'tmethod.log'
     */
    public ProfilerLogAnalysis(String inPath, String methodPath) {
        this.logPath = inPath;
        this.methodPath = methodPath;
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println(
                    "Usage: <tprofiler.log path> <tmethod.log path> <topmethod.log " +
                    "path> <topobject.log path>");
            return;
        }

        ProfilerLogAnalysis analysis = new ProfilerLogAnalysis(args[0], args[1]);
        analysis.reader();
        analysis.printResult(args[2], args[3]);
    }

    /**
     * 读取log,并解析
     */
    private void reader() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(methodPath));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("instrument")) {
                    continue;
                }
                String[] data = line.split(" ");
                if (data.length != 2) {
                    continue;
                }
                methodIdMap.put(Long.parseLong(data[0]), String.valueOf(data[1]));
            }
            reader.close();

            reader = new BufferedReader(new FileReader(logPath));
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("##")) {
                    line = line.substring(line.indexOf(":") + 1, line.length());
                    nano = line.equals("true");
                    continue;
                }

                if ("=".equals(line)) {
                    currentThreadId = -1;
                    doMerge();
                }

                String[] data = line.split("\t");
                if (data.length != 4) {
                    continue;
                }

                merge(Long.parseLong(data[0]), Long.parseLong(data[1]),
                      Long.parseLong(data[2]),
                      Long.parseLong(data[3]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        doMerge();
    }

    /**
     * 合并数据
     */
    private void merge(long threadId, long stackDepth, long methodId, long useTime) {
        if (currentThreadId != threadId) {
            currentThreadId = threadId;
            doMerge();
        }

        threadList.add(new MethodFrame(methodId, useTime, stackDepth));
    }

    /**
     * 合并数据
     */
    private void doMerge() {
        for (int i = 0; i < threadList.size(); i++) {
            MethodFrame m = threadList.get(i);
            long stackNum = m.depth();
            for (int j = i + 1; j < threadList.size(); j++) {
                MethodFrame tmp = threadList.get(j);
                long tmpStack = tmp.depth();
                if (stackNum + 1 == tmpStack) {
                    m.useTime(m.useTime() - tmp.useTime());
                } else if (stackNum >= tmpStack) {
                    break;
                }
            }
        }

        for (MethodFrame m : threadList) {
            if (m.useTime() < 0) {
                break;
            }

            MethodElapsed data = cacheMethodMap.get(m.methodId());
            if (data == null) {
                data = new MethodElapsed(methodIdMap.get(m.methodId()));
                cacheMethodMap.put(m.methodId(), data);
            }

            data.addElapsed(m.useTime());
        }

        threadList.clear();
    }

    /**
     * 输出分析结果
     */
    private void printResult(String topMethodPath, String topObjectPath) {
        List<MethodElapsed> list = new ArrayList<>();
        list.addAll(cacheMethodMap.values());
        Collections.sort(list);

        BufferedWriter topMethodWriter = null;
        BufferedWriter topObjectWriter = null;
        try {
            topMethodWriter = new BufferedWriter(new FileWriter(topMethodPath));
            topObjectWriter = new BufferedWriter(new FileWriter(topObjectPath));
            for (MethodElapsed data : list) {
                StringBuilder sb = new StringBuilder();
                if (nano) {
                    data.setTotalElapsed(
                            MathUtils.divideRoundHalfUp(data.getTotalElapsed(), 1000000));
                }

                sb.append(data.getMethodName());
                sb.append("\t");
                sb.append(data.getInvokedTimes());
                sb.append("\t");
                sb.append(data.getAverageElapsed());
                sb.append("\t");
                sb.append(data.getTotalElapsed());
                sb.append("\n");
                topMethodWriter.write(sb.toString());
                if (data.getMethodName() != null && data.getMethodName().contains(
                        "<init>")) {
                    topObjectWriter.write(sb.toString());
                }
            }
            topMethodWriter.flush();
            topObjectWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (topMethodWriter != null) {
                try {
                    topMethodWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (topObjectWriter != null) {
                try {
                    topObjectWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
