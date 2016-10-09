/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile.thread;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import com.taobao.profile.Manager;
import com.taobao.profile.Profiler;
import com.taobao.profile.config.ProfConfig;
import com.taobao.profile.runtime.MethodCache;

/**
 * 开始时间结束时间控制线程
 *
 * @author shutong.dy
 * @since 2012-1-12
 */
public class TimeControlThread implements Runnable {

    private Object lock = new Object();
    private InnerControlTime startTime;
    private InnerControlTime endTime;

    public TimeControlThread(ProfConfig config) {
        startTime = parse(config.getStartProfTime());
        endTime = parse(config.getEndProfTime());
    }

    /**
     * @param time
     * @return
     */
    public long waitTime(InnerControlTime time) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, time.getHour());
        cal.set(Calendar.MINUTE, time.getMinute());
        cal.set(Calendar.SECOND, time.getSecond());
        long startupTime = cal.getTimeInMillis();
        return startupTime - System.currentTimeMillis();
    }

    /**
     * @param time
     * @return
     */
    public long nextStartTime(InnerControlTime time) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, time.getHour());
        cal.set(Calendar.MINUTE, time.getMinute());
        cal.set(Calendar.SECOND, time.getSecond());
        long startupTime = cal.getTimeInMillis();
        return startupTime - System.currentTimeMillis();
    }

    /**
     * @param time
     */
    private void await(long time) {
        synchronized (lock) {
            try {
                lock.wait(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Thread#run()
     */
    public void run() {
        if (startTime == null || endTime == null) {
            return;
        }
        try {
            // delay 30s
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (true) {
            long time = waitTime(startTime);
            if (time > 0) {
                await(time);
            } else {
                time = waitTime(endTime);
                if (time > 0) {
                    Profiler.clearData();
                    Manager.instance().setTimeFlag(true);
                    await(time);
                    Manager.instance().setTimeFlag(false);
                    MethodCache.flushMethodData();
                } else {
                    time = nextStartTime(startTime);
                    await(time);
                }
            }
        }
    }

    /**
     * @param time
     * @return
     */
    private InnerControlTime parse(String time) {
        if (time == null) {
            return null;
        }

        String[] split = time.trim().split(":");
        if (split.length != 3) {
            return null;
        }

        try {
            return new InnerControlTime(
                    Integer.valueOf(split[0]), Integer.valueOf(split[1]),
                    Integer.valueOf(split[2]));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 映射时间类
     *
     * @author shutong.dy
     * @since 2012-1-12
     */
    private class InnerControlTime {
        private int hour;
        private int minute;
        private int second;

        InnerControlTime(int hour, int minute, int second) {
            this.hour = hour;
            this.minute = minute;
            this.second = second;
        }

        int getHour() {
            return hour;
        }

        void setHour(int hour) {
            this.hour = hour;
        }

        int getMinute() {
            return minute;
        }

        void setMinute(int minute) {
            this.minute = minute;
        }

        int getSecond() {
            return second;
        }

        void setSecond(int second) {
            this.second = second;
        }
    }
}
