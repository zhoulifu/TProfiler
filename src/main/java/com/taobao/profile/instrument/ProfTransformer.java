/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile.instrument;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.taobao.profile.Manager;
import com.taobao.profile.Profiler;
import com.taobao.profile.config.ProfFilter;

/**
 * 自定义ClassFileTransformer,用于转换类字节码
 *
 * @author luqi
 * @since 2010-6-23
 */
public class ProfTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) throws IllegalClassFormatException {

        if (!ProfFilter.needsTransform(loader, className)) {
            return classfileBuffer;
        }

        if (Manager.instance().isDebugMode()) {
            System.out.println(
                    " ---- TProfiler Debug: ClassLoader:" + loader + " ---- class: " +
                    className);
        }

        // 记录注入类数
        Profiler.instrumentClassCount.getAndIncrement();
        try {
            ClassReader reader = new ClassReader(classfileBuffer);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            ClassVisitor visitor = new ProfClassAdapter(writer, className);
            reader.accept(visitor, 0);
            // 生成新类字节码
            return writer.toByteArray();
        } catch (Throwable e) {
            e.printStackTrace();
            // 返回旧类字节码
            return classfileBuffer;
        }
    }
}
