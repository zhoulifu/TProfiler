/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile.config;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 包名过滤器,过滤注入或者不注入的Package
 *
 * @author luqi
 * @since 2010-6-23
 */
public class ProfFilter {

    private static FilterStrategy DEFAULT_STRATEGY = FilterStrategy.INCLUDE;

    static Node root;

    static {
        root = new Node("", DEFAULT_STRATEGY, null);

        // 默认不注入的Package
        addQualifiedName("java", FilterStrategy.EXCLUDE);
        addQualifiedName("sun", FilterStrategy.EXCLUDE);
        addQualifiedName("com.sun", FilterStrategy.EXCLUDE);
        addQualifiedName("org", FilterStrategy.EXCLUDE);
        // 不注入profile本身
        addQualifiedName("com.taobao.profile", FilterStrategy.EXCLUDE);
        addQualifiedName("com.taobao.hsf", FilterStrategy.EXCLUDE);
    }

    /**
     * @param qualifiedName
     */
    public static void addIncludeClass(String qualifiedName) {
        addQualifiedName(qualifiedName, FilterStrategy.INCLUDE);
    }

    /**
     * @param qualifiedName
     */
    public static void addExcludeClass(String qualifiedName) {
        addQualifiedName(qualifiedName, FilterStrategy.EXCLUDE);
    }

    /**
     * @param qualifiedName
     */
    public static void addExcludeClassLoader(String qualifiedName) {
        addQualifiedName(qualifiedName, FilterStrategy.EXCLUDE);
    }

    private static void addQualifiedName(String qualifiedName, FilterStrategy strategy) {
        Node curr = root;
        String[] segments = qualifiedName.split("\\.");
        for (String segment : segments) {
            Node child = curr.getChildByName(segment);
            if (child == null) {
                child = curr.createChildByName(segment);
            }
            curr = child;
        }

        curr.setStrategy(strategy);
    }

    public static boolean needsTransform(ClassLoader classLoader, String className) {
        // return whether the class is included or not excluded.
//        return isIncluded(className) ||
//               !((classLoader != null && isExcludedClassLoader(
//                       classLoader.getClass().getName())) || isExcluded(className));
        // TODO
        return true;
    }

    private static class Node {
        private String name;
        private FilterStrategy strategy;
        private Node parent;
        private List<Node> childrenList;

        Node(String name, Node parent) {
            this(name, parent.strategy, parent);
        }

        Node(String name, FilterStrategy strategy, Node parent) {
            this.name = name;
            this.strategy = strategy;
            this.parent = parent;
        }

        void setStrategy(FilterStrategy strategy) {
            this.strategy = strategy;
        }

        Node createChildByName(String name) {
            if (childrenList == null) {
                childrenList = new CopyOnWriteArrayList<>();
            }

            Node child = new Node(name, this);
            childrenList.add(child);
            return child;
        }

        Node getChildByName(String name) {
            if (childrenList == null) {
                return null;
            }

            for (Node node : childrenList) {
                if (node.name.equals(name)) {
                    return node;
                }
            }

            return null;
        }
    }
}
