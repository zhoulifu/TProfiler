/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile.runtime;

/**
 * 方法信息对象
 *
 * @author xiaodu
 * @since 2010-6-23
 */
class MethodInfo {
    private String className;
    private String methodName;
    private int lineNum;

    MethodInfo() {
    }

    MethodInfo(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    synchronized void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    @Override
    public String toString() {
        return className + ":" + methodName + ":" + lineNum;
    }
}
