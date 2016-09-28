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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分析Sampler生成的Log
 *
 * @author shutong.dy
 * @since 2012-2-9
 */
public class SamplerLogAnalysis {

    private String logPath;
    private Map<String, Integer> originalMethodMap = new HashMap<String, Integer>();
    private List<MethodCount> methodList = new ArrayList<MethodCount>();

    private Map<String, Integer> originalThreadMap = new HashMap<String, Integer>();
    private List<ThreadCount> threadList = new ArrayList<ThreadCount>();

    /**
     * @param inPath
     */
    public SamplerLogAnalysis(String inPath) {
        this.logPath = inPath;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err
                    .println(
                            "Usage: <tsampler.log path> <samplermethodresult.log path> " +
                            "<samplerthreadresult.log path>");
            return;
        }

        SamplerLogAnalysis analysis = new SamplerLogAnalysis(args[0]);
        analysis.reader();
        analysis.printMethodResult(args[1]);
        analysis.printThreadResult(args[2]);
    }

    /**
     * 读取log,并解析
     */
    private void reader() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(logPath)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Thread\t")) {
                    String key = line.substring(0, line.lastIndexOf('\t'));
                    countUp(originalThreadMap, key);
                } else {
                    countUp(originalMethodMap, line);
                }
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
    }

    private void countUp(Map<String, Integer> map, String key) {
        Integer tmpCount = map.get(key);
        if (tmpCount == null) {
            map.put(key, 1);
        } else {
            map.put(key, tmpCount + 1);
        }
    }

    /**
     * 输出分析结果
     *
     * @param outPath
     */
    public void printMethodResult(String outPath) {
        for (Map.Entry<String, Integer> entry : originalMethodMap.entrySet()) {
            methodList.add(new MethodCount(entry.getValue(), entry.getKey()));
        }

        Collections.sort(methodList);

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outPath));
            int count = 0;
            for (MethodCount m : methodList) {
                writer.write(m.getMethodName());
                writer.write("\t");
                writer.write(String.valueOf(m.getCount()));
                writer.write("\n");

                count++;
                if ((count & 31) == 0) {
                    writer.flush();
                }
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 输出分析结果
     *
     * @param outPath
     */
    public void printThreadResult(String outPath) {
        for (Map.Entry<String, Integer> entry : originalThreadMap.entrySet()) {
            String[] tmp = entry.getKey().split("\t");
            threadList.add(new ThreadCount(entry.getValue(), tmp[1], tmp[2], tmp[3]));
        }

        Collections.sort(threadList);

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outPath));
            int count = 0;
            for (ThreadCount t : threadList) {
                writer.write(String.valueOf(t.getThreadId()));
                writer.write("\t");
                writer.write(t.getThreadName());
                writer.write("\t");
                writer.write(t.getThreadState());
                writer.write("\t");
                writer.write(String.valueOf(t.getCount()));
                writer.write("\n");

                count++;
                if ((count & 31) == 0) {
                    writer.flush();
                }
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
