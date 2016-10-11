/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile.thread;

import java.util.concurrent.TimeUnit;

import com.taobao.profile.Manager;
import com.taobao.profile.MethodFrame;
import com.taobao.profile.Profiler;
import com.taobao.profile.config.ProfConfig;
import com.taobao.profile.runtime.ProfStack;
import com.taobao.profile.runtime.ThreadData;
import com.taobao.profile.utils.DailyRollingFileWriter;

/**
 * 将性能分析数据写到log中
 *
 * @author shutong.dy
 * @since 2012-1-11
 */
public class DataDumpThread implements Runnable {
    /**
     * log writer
     */
    private DailyRollingFileWriter fileWriter;
    /**
     * 默认profile时间(s)
     */
    private int eachProfUseTime;
    /**
     * 两次profile间隔时间(s)
     */
    private int eachProfIntervalTime;

    public DataDumpThread(ProfConfig config) {
        // 读取用户配置
        fileWriter = new DailyRollingFileWriter(config.getLogFilePath());
        eachProfUseTime = config.getEachProfUseTime();
        eachProfIntervalTime = config.getEachProfIntervalTime();
    }

    public void run() {
        try {
            while (true) {
                if (Manager.instance().canDump()) {
                    Manager.instance().setProfileFlag(true);
                    TimeUnit.SECONDS.sleep(eachProfUseTime);
                    Manager.instance().setProfileFlag(false);
                    // 等待已开始的End方法执行完成
                    TimeUnit.MILLISECONDS.sleep(500L);

                    dumpProfileData();
                }
                TimeUnit.SECONDS.sleep(eachProfIntervalTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Manager.instance().setProfileFlag(false);
            if (fileWriter != null) {
                fileWriter.closeFile();
            }
            // 等待已开始的End方法执行完成
            try {
                TimeUnit.MILLISECONDS.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Profiler.clearData();
        }
    }

    /**
     * 将profile数据写到log中
     *
     * @return
     */
    private void dumpProfileData() {
        ThreadData[] threadData = Profiler.threadProfile;
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < threadData.length; index++) {
            ThreadData profilerData = threadData[index];
            if (profilerData == null) {
                continue;
            }

            ProfStack<MethodFrame> profile = profilerData.profileData;
            while (profile.size() > 0) {
                MethodFrame frame = profile.pop();
                sb.append(index).append('\t')
                  .append(frame.depth()).append('\t')
                  .append(frame.methodId()).append('\t')
                  .append(frame.useTime()).append('\n');
                fileWriter.append(sb.toString());
                sb.setLength(0);
            }

            fileWriter.flushAppend();
            profilerData.clear();
        }

        fileWriter.append("=\n");
        fileWriter.flushAppend();
    }
}
