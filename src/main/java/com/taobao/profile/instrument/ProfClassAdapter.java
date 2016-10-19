/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile.instrument;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.taobao.profile.Manager;
import com.taobao.profile.Profiler;
import com.taobao.profile.utils.StringUtils;

/**
 * ASM类配置器
 *
 * @author luqi
 * @since 2010-6-23
 */
public class ProfClassAdapter extends ClassVisitor {
    /**
     * Getter/setter method name cache.
     */
    private List<String> pojoMethodNames = new ArrayList<>();
    private String className;

    public ProfClassAdapter(ClassVisitor visitor, String className) {
        super(Opcodes.ASM5, visitor);
        this.className = className;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature,
            Object value) {
        String upper = StringUtils.upperCaseFirstLetter(name);
        String getter = "get" + upper;
        String setter = "set" + upper;
        String boolGetter = "is" + upper;
        pojoMethodNames.add(getter);
        pojoMethodNames.add(setter);
        pojoMethodNames.add(boolGetter);

        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int arg, String name, String descriptor,
            String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(arg, name, descriptor, signature,
                                             exceptions);
        // Ignore getter/setter and the static initialization methods
        if ((Manager.isIgnoreGetSetMethod() && pojoMethodNames.contains(name))
            || ("<clinit>".equals(name))) {
            return mv;
        }

        Profiler.increaseMethodCount();
        return new ProfMethodAdapter(mv, className, name);
    }
}
