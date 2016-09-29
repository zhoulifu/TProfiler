/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile;

import java.util.concurrent.atomic.AtomicInteger;

import com.taobao.profile.runtime.ThreadData;

/**
 * 此类收集应用代码的运行时数据
 *
 * @author luqi
 * @since 2010-6-23
 */
public class Profiler {
    private final static int size = 65535;
    /**
     * 注入类数
     */
    public static AtomicInteger instrumentClassCount = new AtomicInteger(0);
    /**
     * 注入方法数
     */
    public static AtomicInteger instrumentMethodCount = new AtomicInteger(0);
    /**
     * 线程数组
     */
    public static ThreadData[] threadProfile = new ThreadData[size];

    /**
     * 方法开始时调用,采集开始时间
     *
     * @param methodId
     */
    public static void Start(int methodId) {
        if (!Manager.instance().canProfile()) {
            return;
        }
        long threadId = Thread.currentThread().getId();
        if (threadId >= size) {
            return;
        }

        long startTime;
        if (Manager.isNeedNanoTime()) {
            startTime = System.nanoTime();
        } else {
            startTime = System.currentTimeMillis();
        }
        try {
            ThreadData thrData = threadProfile[(int) threadId];
            if (thrData == null) {
                thrData = new ThreadData();
                threadProfile[(int) threadId] = thrData;
            }

            thrData.stackFrame.push(new MethodFrame(methodId, startTime, thrData.stackNum));
            thrData.stackNum++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 方法退出时调用,采集结束时间
     *
     * @param methodId
     */
    public static void End(int methodId) {
        if (!Manager.instance().canProfile()) {
            return;
        }
        long threadId = Thread.currentThread().getId();
        if (threadId >= size) {
            return;
        }

        long endTime;
        if (Manager.isNeedNanoTime()) {
            endTime = System.nanoTime();
        } else {
            endTime = System.currentTimeMillis();
        }
        try {
            ThreadData thrData = threadProfile[(int) threadId];
            if (thrData == null || thrData.stackNum <= 0 ||
                thrData.stackFrame.size() == 0) {
                // 没有执行start,直接执行end/可能是异步停止导致的
                return;
            }
            // 栈太深则抛弃部分数据
            if (thrData.profileData.size() > 20000) {
                thrData.stackNum--;
                thrData.stackFrame.pop();
                return;
            }
            thrData.stackNum--;
            MethodFrame frameData = thrData.stackFrame.pop();
            long id = frameData.methodId();
            if (methodId != id) {
                return;
            }
            long elapsed = endTime - frameData.useTime();
            if (Manager.isNeedNanoTime()) {
                if (elapsed > 500000) {
                    frameData.useTime(elapsed);
                    thrData.profileData.push(frameData);
                }
            } else if (elapsed > 1) {
                frameData.useTime(elapsed);
                thrData.profileData.push(frameData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearData() {
        for (int index = 0; index < threadProfile.length; index++) {
            ThreadData profilerData = threadProfile[index];
            if (profilerData == null) {
                continue;
            }
            profilerData.clear();
        }
    }
}
