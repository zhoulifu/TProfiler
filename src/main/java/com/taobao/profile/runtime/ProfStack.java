/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile.runtime;

import java.util.Arrays;

/**
 * 自定义栈
 *
 * @author xiaodu
 * @since 2010-6-23
 */
public class ProfStack<E> {

    protected Object[] elementData;
    protected int elementCount;

    public ProfStack() {
        elementData = new Object[200];
    }

    private void ensureCapacityHelper(int minCapacity) {
        int oldCapacity = elementData.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 2);
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            elementData = Arrays.copyOf(elementData, newCapacity);
        }
    }

    public E push(E item) {
        ensureCapacityHelper(elementCount + 1);
        elementData[elementCount++] = item;
        return item;
    }

    public E pop() {
        E obj;
        obj = peek();
        removeElementAt(elementCount - 1);
        return obj;
    }

    public E peek() {
        if (elementCount == 0)
            return null;
        return elementAt(elementCount - 1);
    }

    public void clear() {
        for (int i = 0; i < elementCount; i++)
            elementData[i] = null;
        elementCount = 0;
    }

    public void removeElementAt(int index) {
        if (index >= elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
        } else if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        int j = elementCount - index - 1;
        if (j > 0) {
            System.arraycopy(elementData, index + 1, elementData, index, j);
        }
        elementCount--;
        elementData[elementCount] = null; /* to let gc do its work */
    }

    @SuppressWarnings("unchecked")
    public E elementAt(int index) {
        if (index >= elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
        }

        return (E) elementData[index];
    }

    public int size() {
        return elementCount;
    }
}
