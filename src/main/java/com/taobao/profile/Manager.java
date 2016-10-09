/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadFactory;

import com.taobao.profile.config.ProfConfig;
import com.taobao.profile.config.ProfFilter;
import com.taobao.profile.thread.DataDumpThread;
import com.taobao.profile.thread.InnerSocketThread;
import com.taobao.profile.thread.SamplerThread;
import com.taobao.profile.thread.TimeControlThread;

/**
 * 管理类,单例实现
 *
 * @author shutong.dy
 * @since 2012-1-9
 */
public class Manager {
    /**
     * 远程连接端口
     */
    public static int PORT;
    /**
     * 默认方法log位置
     */
    public static String METHOD_LOG_PATH;
    /**
     * 是否用纳秒采集
     */
    private static boolean NEED_NANO_TIME;
    /**
     * 是否忽略get/set方法
     */
    private static boolean IGNORE_GET_SET_METHOD;
    /**
     * Manager
     */
    private static Manager manager = new Manager();

    /**
     * profile配置
     */
    private ProfConfig profConfig;

    /**
     * 时间标记.当前时间大于开始时间小于结束时间,则可以profile. default:false 不可以profile
     */
    private volatile boolean timeFlag = false;
    /**
     * 开关标记.远程开始或结束的开关. default:true 打开状态
     */
    private volatile boolean switchFlag = true;
    /**
     * profile标记.是否可以profile. default:false 不可以profile
     */
    private volatile boolean profileFlag = false;
    /**
     * 启动时间是否大于采集结束时间
     */
    private boolean moreThanEndTime;
    /**
     * 是否进入调试模式
     */
    private boolean isDebugMode;

    /**
     * 私有构造器
     */
    private Manager() {
    }

    /**
     * @return
     */
    public static Manager instance() {
        return manager;
    }

    /**
     * @return the needNanoTime
     */
    public static boolean isNeedNanoTime() {
        return NEED_NANO_TIME;
    }

    /**
     * @return the ignoreGetSetMethod
     */
    public static boolean isIgnoreGetSetMethod() {
        return IGNORE_GET_SET_METHOD;
    }

    /**
     * 初始化配置
     */
    public void initialization() {
        profConfig = new ProfConfig();
        NEED_NANO_TIME = profConfig.isNeedNanoTime();
        IGNORE_GET_SET_METHOD = profConfig.isIgnoreGetSetMethod();
        METHOD_LOG_PATH = profConfig.getMethodFilePath();
        // 判断启动时间是否大于采集结束时间 2012-05-25
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        String now = df.format(new Date());
        moreThanEndTime = (now.compareTo(profConfig.getEndProfTime()) > 0);
        isDebugMode = profConfig.isDebugMode();
        PORT = profConfig.getPort();

        setProfFilter();
    }

    /**
     * @param value the profileFlag to set
     */
    public void setProfileFlag(boolean value) {
        profileFlag = value;
    }

    /**
     * 判断当前是否可以采集数据
     *
     * @return
     */
    public boolean canProfile() {
        return profileFlag;
    }

    /**
     * @return
     */
    public boolean getSwitchFlag() {
        return switchFlag;
    }

    /**
     * @param value the switchFlag to set
     */
    public void setSwitchFlag(boolean value) {
        switchFlag = value;
    }

    /**
     * @return the timeFlag
     */
    public boolean getTimeFlag() {
        return timeFlag;
    }

    /**
     * @param value the timeFlag to set
     */
    public void setTimeFlag(boolean value) {
        timeFlag = value;
    }

    /**
     * 判断当前是否可以dump数据
     *
     * @return
     */
    public boolean canDump() {
        return timeFlag && switchFlag;
    }

    /**
     * 启动时间是否大于profile结束时间
     *
     * @return
     */
    public boolean isMoreThanEndTime() {
        return moreThanEndTime;
    }

    /**
     * @return the isDebugMode
     */
    public boolean isDebugMode() {
        return isDebugMode;
    }

    /**
     * 设置包名过滤器
     */
    private void setProfFilter() {
        String classLoader = profConfig.getExcludeClassLoader();
        if (classLoader != null && classLoader.trim().length() > 0) {
            String[] _classLoader = classLoader.split(";");
            for (String pack : _classLoader) {
                ProfFilter.addExcludeClassLoader(pack);
            }
        }
        String include = profConfig.getIncludePackageStartsWith();
        if (include != null && include.trim().length() > 0) {
            String[] _includes = include.split(";");
            for (String pack : _includes) {
                ProfFilter.addIncludeClass(pack);
            }
        }
        String exclude = profConfig.getExcludePackageStartsWith();
        if (exclude != null && exclude.trim().length() > 0) {
            String[] _excludes = exclude.split(";");
            for (String pack : _excludes) {
                ProfFilter.addExcludeClass(pack);
            }
        }
    }

    /**
     * 启动内部线程
     */
    public void startupThread() {
        factory.newThread(new TimeControlThread(profConfig)).start();
        factory.newThread(new InnerSocketThread()).start();
        factory.newThread(new DataDumpThread(profConfig)).start();
        factory.newThread(new SamplerThread(profConfig)).start();
    }

    private ThreadFactory factory = new ThreadFactory() {
        private final String group = "TProfiler";

        @Override
        public Thread newThread(Runnable r) {
            String threadName = r.getClass().getSimpleName();
            Thread thread = new Thread(r, group + "-" + threadName);
            thread.setDaemon(true);
            return thread;
        }
    };
}
