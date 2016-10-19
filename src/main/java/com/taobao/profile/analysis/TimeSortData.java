/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile.analysis;

import com.taobao.profile.utils.MathUtils;

/**
 * 可排序数据对象
 *
 * @author shutong.dy
 * @since 2012-1-11
 */
public class TimeSortData implements Comparable<TimeSortData> {

    private long totalElapsed;
    private String methodName;
    private int invokedTimes;

    public TimeSortData() {
    }

    public TimeSortData(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public long getTotalElapsed() {
        return totalElapsed;
    }

    public void setTotalElapsed(long totalElapsed) {
        this.totalElapsed = totalElapsed;
    }

    public int getInvokedTimes() {
        return invokedTimes;
    }

    public void setInvokedTimes(int invokedTimes) {
        this.invokedTimes = invokedTimes;
    }

    public void addElapsed(long elapsed) {
        invokedTimes++;
        totalElapsed += elapsed;
    }


    public long getAverageElapsed() {
        return MathUtils.divideRoundHalfUp(getTotalElapsed(), getInvokedTimes());
    }

    public int compareTo(TimeSortData o) {
        return Long.compare(o.totalElapsed, this.totalElapsed);
    }
}
