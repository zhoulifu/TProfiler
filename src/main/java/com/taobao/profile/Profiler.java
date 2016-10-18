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
    private final static int DROP_THRESHOLD = 20000;
    private final static int SIZE = 65535;
    /**
     * 线程数组
     */
    public static ThreadData[] threadProfile = new ThreadData[SIZE];
    /**
     * 注入类数
     */
    private static AtomicInteger classCounter = new AtomicInteger(0);
    /**
     * 注入方法数
     */
    private static AtomicInteger methodCounter = new AtomicInteger(0);

    public static void increaseMethodCount() {
        methodCounter.incrementAndGet();
    }

    public static void increaseClassCount() {
        classCounter.incrementAndGet();
    }

    public static int getClassCount() {
        return classCounter.intValue();
    }

    public static int getMethodCount() {
        return methodCounter.intValue();
    }

    /**
     * 方法开始时调用,采集开始时间
     *
     * @param methodId current method id
     */
    public static void Start(int methodId) {
        if (!Manager.instance().canProfile()) {
            return;
        }
        long threadId = Thread.currentThread().getId();
        if (threadId >= SIZE) {
            return;
        }

        long startTime = currentTime();
        try {
            ThreadData thrData = threadProfile[(int) threadId];
            if (thrData == null) {
                thrData = new ThreadData();
                threadProfile[(int) threadId] = thrData;
            }

            thrData.stackFrame.push(
                    new MethodFrame(methodId, startTime, thrData.stackNum));
            thrData.stackNum++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 方法退出时调用,采集结束时间
     *
     * @param methodId current method id
     */
    public static void End(int methodId) {
        if (!Manager.instance().canProfile()) {
            return;
        }
        long threadId = Thread.currentThread().getId();
        if (threadId >= SIZE) {
            return;
        }

        long endTime = currentTime();
        try {
            ThreadData thrData = threadProfile[(int) threadId];
            if (thrData == null || thrData.stackNum <= 0 ||
                thrData.stackFrame.size() == 0) {
                // 没有执行start,直接执行end/可能是异步停止导致的
                return;
            }

            thrData.stackNum--;
            MethodFrame frameData = thrData.stackFrame.pop();

            // Drop frames if stack depth exceeds the threshold. Actually it will drop
            // frames at the bottom of 'stackFrame'(basically equal to method call
            // stack) because of doing this check using 'profileData'.
            if ((thrData.profileData.size() > DROP_THRESHOLD) ||
                (methodId != frameData.methodId())) {
                return;
            }

            long elapsed = endTime - frameData.useTime();
            if (elapsedTimeAtLeastOneMillisecond(elapsed)) {
                frameData.useTime(elapsed);
                thrData.profileData.push(frameData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static long currentTime() {
        return Manager.isNeedNanoTime() ? System.nanoTime() : System.currentTimeMillis();
    }

    /**
     * Tell whether the elapsed time is at least one millisecond.(more exactly, time
     * more than half millisecond)
     *
     * @return true if elapsed more than half millisecond
     */
    private static boolean elapsedTimeAtLeastOneMillisecond(long elapsed) {
        return Manager.isNeedNanoTime() ? (elapsed > 500000) : (elapsed >= 1);
    }

    public static void clearData() {
        for (ThreadData profilerData : threadProfile) {
            if (profilerData == null) {
                continue;
            }

            profilerData.clear();
        }
    }
}
