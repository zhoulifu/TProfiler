/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile.analysis;

import com.taobao.profile.utils.MathUtils;

class MethodElapsed implements Comparable<MethodElapsed> {

    private long totalElapsed;
    private String methodName;
    private int invokedTimes;

    public MethodElapsed() {
    }

    MethodElapsed(String methodName) {
        this.methodName = methodName;
    }

    String getMethodName() {
        return methodName;
    }

    void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    long getTotalElapsed() {
        return totalElapsed;
    }

    void setTotalElapsed(long totalElapsed) {
        this.totalElapsed = totalElapsed;
    }

    int getInvokedTimes() {
        return invokedTimes;
    }

    void setInvokedTimes(int invokedTimes) {
        this.invokedTimes = invokedTimes;
    }

    void addElapsed(long elapsed) {
        invokedTimes++;
        totalElapsed += elapsed;
    }

    long getAverageElapsed() {
        return MathUtils.divideRoundHalfUp(getTotalElapsed(), getInvokedTimes());
    }

    public int compareTo(MethodElapsed o) {
        return Long.compare(o.totalElapsed, this.totalElapsed);
    }
}
