/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile.instrument;

import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.taobao.profile.runtime.MethodCache;

/**
 * ASM方法适配器
 *
 * @author luqi
 * @since 2010-6-23
 */
public class ProfMethodVisitor extends MethodVisitor {

    private int methodId;

    public ProfMethodVisitor(MethodVisitor visitor, String className, String methodName) {
        super(ASM5, visitor);
        methodId = MethodCache.Request(className, methodName);
    }

    public void visitCode() {
        this.visitLdcInsn(methodId);
        this.visitMethodInsn(INVOKESTATIC, "com/taobao/profile/Profiler", "Start",
                             "(I)V", false);
        super.visitCode();
    }

    public void visitLineNumber(final int line, final Label start) {
        MethodCache.UpdateLineNum(methodId, line);
        super.visitLineNumber(line, start);
    }

    public void visitInsn(int inst) {
        switch (inst) {
            case Opcodes.ARETURN:
            case Opcodes.DRETURN:
            case Opcodes.FRETURN:
            case Opcodes.IRETURN:
            case Opcodes.LRETURN:
            case Opcodes.RETURN:
            case Opcodes.ATHROW:
                this.visitLdcInsn(methodId);
                this.visitMethodInsn(INVOKESTATIC, "com/taobao/profile/Profiler", "End",
                                     "(I)V", false);
                break;
            default:
                break;
        }

        super.visitInsn(inst);
    }
}
